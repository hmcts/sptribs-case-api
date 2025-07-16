package uk.gov.hmcts.sptribs.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CreateTestCaseTest {

    @InjectMocks
    private CreateTestCase createTestCase;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        createTestCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("create-test-case");
    }

    @Test
    void shouldMoveCaseIntoChosenStateAndCreateTestCase() throws JsonProcessingException {
        final CaseData caseData =
            CaseData.builder()
                .caseStatus(CaseManagement)
                .build();
        final AppsConfig.AppsDetails appsDetails = new AppsConfig.AppsDetails();
        appsDetails.setCaseType("CriminalInjuriesCompensation");
        appsDetails.setJurisdiction("ST_CIC");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Document expectedCdamUploadedDocument = new Document();
        final Document.DocumentLink documentLink = new Document.DocumentLink();
        documentLink.href = "dmstore-url/doc-id";
        final Document.DocumentLink binaryDocumentLink = new Document.DocumentLink();
        binaryDocumentLink.href = "dmstore-url/doc-id/binary";
        final Document.Links links = new Document.Links();
        links.self = documentLink;
        links.binary = binaryDocumentLink;
        expectedCdamUploadedDocument.setLinks(links);
        expectedCdamUploadedDocument.setOriginalDocumentName("sample_file.pdf");

        final List<Document> expectedDocuments = new ArrayList<>();
        expectedDocuments.add(expectedCdamUploadedDocument);

        final uk.gov.hmcts.ccd.sdk.type.Document expectedUploadDocument = uk.gov.hmcts.ccd.sdk.type.Document.builder()
            .url(expectedCdamUploadedDocument.links.self.href)
            .filename(expectedCdamUploadedDocument.originalDocumentName)
            .categoryId("A")
            .binaryUrl(expectedCdamUploadedDocument.links.binary.href)
            .build();

        final CaseworkerCICDocument expectedCICDocument = CaseworkerCICDocument.builder()
            .documentLink(expectedUploadDocument)
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentEmailContent("This is a test document uploaded during create case journey")
            .build();
        final ListValue<CaseworkerCICDocument> expectedCICDocumentListValue =
            ListValue.<CaseworkerCICDocument>builder().value(expectedCICDocument).build();

        UploadResponse expectedResponse = new UploadResponse();
        expectedResponse.setDocuments(expectedDocuments);
        when(appsConfig.getApps()).thenReturn(List.of(appsDetails));
        when(mapper.readValue(anyString(), eq(CaseData.class))).thenReturn(caseData());
        when(caseDocumentClientApi.uploadDocuments(any(), any(), any())).thenReturn(expectedResponse);

        AboutToStartOrSubmitResponse<CaseData, State> response = createTestCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(CaseManagement);
        assertThat(response.getData().getHyphenatedCaseRef()).isEqualTo(TEST_CASE_ID_HYPHENATED);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(1);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().getFirst().getValue()).isEqualTo(expectedCICDocument);
    }

    @Test
    void shouldSubmitSupplementaryDataToCcdWhenSubmittedEventTriggered() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        createTestCase.submitted(caseDetails, caseDetails);

        verify(ccdSupplementaryDataService).submitSupplementaryDataToCcd(TEST_CASE_ID.toString());
    }
}
