package uk.gov.hmcts.sptribs.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
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
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.AddSystemUpdateRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
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

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    private AutoCloseable autoCloseableMocks;


    @BeforeEach
    public void setUp() {
        autoCloseableMocks = MockitoAnnotations.openMocks(this);

        cicAppDetail = new AppsConfig.AppsDetails();
        cicAppDetail.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        cicAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setSubmitEvent("citizen-cic-submit-dss-application");

        cicAppDetail.setEventIds(eventsConfig);
        cicSubmitCaseEvent.setSni5511Enabled(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseableMocks.close();
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(CITIZEN));

        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));
        cicSubmitCaseEvent.setDssSubmitCaseEnabled(true);

        cicSubmitCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CommonConstants.ST_CIC_CASE_TYPE).getEventIds()
                .getSubmitEvent());
    }

    @Test
    void shouldUpdateCaseDetails() {
        final EdgeCaseDocument dssDoc = new EdgeCaseDocument();
        dssDoc.setDocumentLink(Document.builder().build());
        final ListValue<EdgeCaseDocument> listValue = new ListValue<>();
        listValue.setValue(dssDoc);
        final DssCaseData dssCaseData = DssCaseData.builder()
            .caseTypeOfApplication(CASE_DATA_CIC_ID)
            .otherInfoDocuments(List.of(listValue))
            .supportingDocuments(List.of(listValue))
            .tribunalFormDocuments(List.of(listValue))
            .subjectFullName(TEST_FIRST_NAME)
            .representation(YesOrNo.YES)
            .representationQualified(YesOrNo.YES)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();

        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDssCaseData(dssCaseData);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setData(caseData);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            cicSubmitCaseEvent.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getRepresentativeContactDetailsPreference()).isEqualTo(ContactPreferenceType.EMAIL);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue().getDocumentCategory())
            .isIn(DocumentType.DSS_OTHER, DocumentType.DSS_SUPPORTING, DocumentType.DSS_TRIBUNAL_FORM);
    }
}
