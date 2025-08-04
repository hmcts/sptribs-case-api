package uk.gov.hmcts.sptribs.services.cdam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_FILE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(MockitoExtension.class)
public class DssCaseDocumentClientTest {

    @InjectMocks
    DssCaseDocumentClient caseDocumentClient;

    @Mock
    CaseDocumentClientApi caseDocumentClientApi;

    @Test
    public void shouldUploadDocuments() {
        final UploadResponse response = new UploadResponse();
        when(caseDocumentClientApi.uploadDocuments(eq("authorisation"), eq("serviceAuthorisation"),
            any(DocumentUploadRequest.class))).thenReturn(response);

        final UploadResponse uploadResponse = caseDocumentClient.uploadDocuments("authorisation", "serviceAuthorisation",
            "CriminalInjuriesCompensation", "ST_CIC", new ArrayList<>());

        assertEquals(response, uploadResponse);
        assertEquals(response.getDocuments(), uploadResponse.getDocuments());
    }

    @Test
    void shouldUploadDocumentsWithClassification() throws IOException {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final MockMultipartFile multipartFile = new MockMultipartFile(
            JSON_FILE_TYPE,
            CASE_DATA_FILE_CIC,
            JSON_CONTENT_TYPE,
            caseDataJson.getBytes()
        );

        final List<MultipartFile> multipartFileList = new ArrayList<>();

        multipartFileList.add(multipartFile);

        final UploadResponse response = new UploadResponse();

        when(caseDocumentClientApi.uploadDocuments(any(), any(),any(DocumentUploadRequest.class)))
            .thenReturn(response);

        final UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            AUTHORIZATION,
            SERVICE_AUTHORIZATION,
            "CriminalInjuriesCompensation",
            ST_CIC_JURISDICTION,
            multipartFileList,
            Classification.PUBLIC);

        assertNotNull(uploadResponse);
        assertEquals(response,uploadResponse);
        assertEquals(response.getDocuments(), uploadResponse.getDocuments());
    }

    @Test
    public void shouldDeleteDocument() {
        final UUID docUUID = UUID.randomUUID();

        caseDocumentClient.deleteDocument("authorisation", "serviceAuthorisation", docUUID, true);

        verify(caseDocumentClientApi).deleteDocument(eq("authorisation"), eq("serviceAuthorisation"),
            eq(docUUID), eq(true));
    }
}
