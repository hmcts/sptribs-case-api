package uk.gov.hmcts.sptribs.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.util.ReflectionTestUtils.setField;
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

@ExtendWith(MockitoExtension.class)
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
    void shouldAddConfigurationToConfigBuilder() {
        setField(cicSubmitCaseEvent, "dssSubmitCaseEnabled", true);

        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));

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
        caseDetails.getData().getDssCaseData().setSubjectFullName(TEST_FIRST_NAME);
        caseDetails.getData().getDssCaseData().setRepresentativeFullName(TEST_FIRST_NAME);

        cicSubmitCaseEvent.configure(configBuilder);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = cicSubmitCaseEvent.aboutToSubmit(
            caseDetails,
            beforeCaseDetails
        );

        Assertions.assertEquals(State.DSS_Submitted, aboutToSubmitResponse.getState());
    }

    @Test
    void shouldUpdateCaseDetails() {
        EdgeCaseDocument dssDoc = new EdgeCaseDocument();
        dssDoc.setDocumentLink(Document.builder().build());
        ListValue<EdgeCaseDocument> listValue = new ListValue<>();
        listValue.setValue(dssDoc);
        DssCaseData dssCaseData = DssCaseData.builder()
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

        CicCase cicCase = CicCase.builder().build();
        CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDssCaseData(dssCaseData);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setData(caseData);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicSubmitCaseEvent.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(2);
        assertThat(response.getData().getCicCase().getRepresentativeContactDetailsPreference()).isEqualTo(ContactPreferenceType.EMAIL);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue().getDocumentCategory())
            .isIn(DocumentType.DSS_OTHER, DocumentType.DSS_SUPPORTING, DocumentType.DSS_TRIBUNAL_FORM);
    }
}
