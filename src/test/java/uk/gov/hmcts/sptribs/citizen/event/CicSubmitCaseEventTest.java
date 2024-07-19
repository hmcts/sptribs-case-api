package uk.gov.hmcts.sptribs.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_UPDATE_CASE_EMAIL_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CicSubmitCaseEventTest {
    final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

    @InjectMocks
    private CicSubmitCaseEvent cicSubmitCaseEvent;

    @Mock
    private DssApplicationReceivedNotification dssApplicationReceivedNotification;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AppsConfig.AppsDetails cicAppDetail;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private CcdSupplementaryDataService ccdSupplementaryDataService;

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
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseableMocks.close();
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));

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
            .isEqualTo(DocumentType.DSS_OTHER);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue().getDate()).isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDocumentCategory())
            .isEqualTo(DocumentType.DSS_SUPPORTING);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate()).isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDocumentCategory())
            .isEqualTo(DocumentType.DSS_TRIBUNAL_FORM);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate()).isEqualTo(LocalDate.now());
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags().getPartyName()).isEqualTo(TEST_FIRST_NAME);
        assertThat(response.getData().getSubjectFlags().getRoleOnCase()).isEqualTo("subject");
        assertThat(response.getData().getRepresentativeFlags()).isNotNull();
        assertThat(response.getData().getRepresentativeFlags().getPartyName()).isEqualTo(TEST_SOLICITOR_NAME);
        assertThat(response.getData().getRepresentativeFlags().getRoleOnCase()).isEqualTo("Representative");
    }

    @Test
    void shouldSubmitEventThroughSubmittedCallbackWithNotification() throws IOException {
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

        caseDetails.getData().getDssCaseData().setSubjectFullName(TEST_FIRST_NAME);
        caseDetails.getData().getDssCaseData().setSubjectEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);
        caseDetails.getData().getDssCaseData().setRepresentativeFullName(TEST_FIRST_NAME);
        caseDetails.getData().getDssCaseData().setRepresentativeEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);

        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));
        cicSubmitCaseEvent.configure(configBuilder);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = cicSubmitCaseEvent.aboutToSubmit(
            caseDetails,
            beforeCaseDetails
        );

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(State.DSS_Submitted);
        assertThat(aboutToSubmitResponse.getData().getDssQuestion1()).isEqualTo("Full Name");
        assertThat(aboutToSubmitResponse.getData().getDssQuestion2()).isEqualTo("Date of Birth");
        assertThat(aboutToSubmitResponse.getData().getDssAnswer1()).isEqualTo("case_data.dssCaseDataSubjectFullName");
        assertThat(aboutToSubmitResponse.getData().getDssAnswer2()).isEqualTo("case_data.dssCaseDataSubjectDateOfBirth");
        assertThat(aboutToSubmitResponse.getData().getDssHeaderDetails()).isEqualTo("Subject of this case");

        SubmittedCallbackResponse submittedResponse = cicSubmitCaseEvent.submitted(caseDetails, beforeCaseDetails);

        assertThat(submittedResponse.getConfirmationHeader())
            .isEqualTo("# Application Received \n## A notification has been sent to: Subject, Representative");
        verify(dssApplicationReceivedNotification).sendToSubject(any(DssCaseData.class), any());
        verify(dssApplicationReceivedNotification).sendToRepresentative(any(DssCaseData.class), any());
    }

    @Test
    void shouldSubmitEventThroughSubmittedCallbackWithoutNotificationParties() throws IOException {
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

        caseDetails.getData().getDssCaseData().setSubjectFullName(TEST_FIRST_NAME);
        caseDetails.getData().getDssCaseData().setRepresentativeFullName(TEST_FIRST_NAME);

        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));
        cicSubmitCaseEvent.configure(configBuilder);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = cicSubmitCaseEvent.aboutToSubmit(
            caseDetails,
            beforeCaseDetails
        );

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(State.DSS_Submitted);
        assertThat(aboutToSubmitResponse.getData().getDssQuestion1()).isEqualTo("Full Name");
        assertThat(aboutToSubmitResponse.getData().getDssQuestion2()).isEqualTo("Date of Birth");
        assertThat(aboutToSubmitResponse.getData().getDssAnswer1()).isEqualTo("case_data.dssCaseDataSubjectFullName");
        assertThat(aboutToSubmitResponse.getData().getDssAnswer2()).isEqualTo("case_data.dssCaseDataSubjectDateOfBirth");
        assertThat(aboutToSubmitResponse.getData().getDssHeaderDetails()).isEqualTo("Subject of this case");

        SubmittedCallbackResponse submittedResponse = cicSubmitCaseEvent.submitted(caseDetails, beforeCaseDetails);

        assertThat(submittedResponse.getConfirmationHeader())
            .isEqualTo("# Application Received %n##");
    }

    @Test
    void shouldCatchErrorIfSendEmailNotificationFails() {
        CaseData caseData = CaseData.builder().build();
        DssCaseData dssCaseData = DssCaseData.builder().build();
        dssCaseData.setSubjectEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);
        dssCaseData.setRepresentativeEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);
        caseData.setDssCaseData(dssCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        doThrow(NotificationException.class)
            .when(dssApplicationReceivedNotification)
            .sendToSubject(any(DssCaseData.class), anyString());
        doThrow(NotificationException.class)
            .when(dssApplicationReceivedNotification)
            .sendToRepresentative(any(DssCaseData.class), anyString());

        SubmittedCallbackResponse response = cicSubmitCaseEvent.submitted(details, details);

        assertThat(response.getConfirmationHeader())
            .contains("# Application Received notification failed %n## Please resend the notification");
    }

    @Test
    void shouldSubmitSupplementaryDataToCcdWhenSubmittedEventTriggered() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        cicSubmitCaseEvent.submitted(caseDetails, caseDetails);

        verify(ccdSupplementaryDataService).submitSupplementaryDataRequestToCcd(TEST_CASE_ID.toString());
    }

}
