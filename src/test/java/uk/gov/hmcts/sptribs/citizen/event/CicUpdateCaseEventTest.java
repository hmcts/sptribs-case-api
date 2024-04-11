package uk.gov.hmcts.sptribs.citizen.event;

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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CicUpdateCaseEventTest {
    @InjectMocks
    private CicUpdateCaseEvent cicUpdateCaseEvent;

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

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent("citizen-cic-update-dss-application");

        cicAppDetail.setEventIds(eventsConfig);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(appsConfig.getApps()).thenReturn(singletonList(cicAppDetail));

        cicUpdateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("citizen-cic-update-dss-application");
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getDescription)
            .contains("Application update (cic)");
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getName)
            .contains("Update case (cic)");
    }
}
