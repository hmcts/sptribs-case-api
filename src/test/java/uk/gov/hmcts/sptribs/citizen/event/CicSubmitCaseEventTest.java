package uk.gov.hmcts.sptribs.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.AddSystemUpdateRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_UPDATE_CASE_EMAIL_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CicSubmitCaseEventTest {
    final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

    @InjectMocks
    private CicSubmitCaseEvent cicSubmitCaseEvent;

    @Mock
    private AddSystemUpdateRole addSystemUpdateRole;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AppsConfig.AppsDetails cicAppDetail;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        cicAppDetail = new AppsConfig.AppsDetails();
        cicAppDetail.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        cicAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
        AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setSubmitEvent("citizen-cic-submit-dss-application");

        cicAppDetail.setEventIds(eventsConfig);

    }

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(CITIZEN_CIC));

        when(appsConfig.getApps()).thenReturn(Arrays.asList(cicAppDetail));

        cicSubmitCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CommonConstants.ST_CIC_CASE_TYPE).getEventIds()
                          .getSubmitEvent());
    }

    @Test
    void shouldSubmitEventThroughAboutToSubmit() throws IOException {
        final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseBeforeData = mapper.readValue(caseDataJson, CaseData.class);

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        beforeCaseDetails.setData(caseBeforeData);
        beforeCaseDetails.setState(State.Submitted);
        beforeCaseDetails.setId(TEST_CASE_ID);
        beforeCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);
        caseDetails.setData(caseData);
        caseDetails.setState(State.Submitted);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        caseDetails.getData().getDssCaseData().setSubjectEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);

        when(appsConfig.getApps()).thenReturn(Arrays.asList(cicAppDetail));

        cicSubmitCaseEvent.configure(configBuilder);

        AboutToStartOrSubmitResponse<Object, Object> submitResponseBuilder
            = AboutToStartOrSubmitResponse.builder().data(caseData).state(State.Submitted).build();


        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = cicSubmitCaseEvent.aboutToSubmit(
            caseDetails,
            beforeCaseDetails
        );

        Assertions.assertEquals(aboutToSubmitResponse.getData(), caseDetails.getData());
        Assertions.assertEquals(aboutToSubmitResponse.getState(), caseDetails.getState());
        Assertions.assertNotEquals(submitResponseBuilder.getData(), beforeCaseDetails.getData());
        Assertions.assertEquals(submitResponseBuilder.getState(), caseDetails.getState());
    }
}
