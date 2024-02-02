package uk.gov.hmcts.sptribs.services.cdam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
public class DssCaseDocumentClientTest {

    @InjectMocks
    DssCaseDocumentClient caseDocumentClient;

    @Mock
    CaseDocumentClientApi caseDocumentClientApi;

    @Test
    public void shouldUploadDocuments() {
        // Given
        final UploadResponse response = new UploadResponse();
        when(caseDocumentClientApi.uploadDocuments(eq("authorisation"), eq("serviceAuthorisation"),
            any(DocumentUploadRequest.class))).thenReturn(response);

        // When
        final UploadResponse uploadResponse = caseDocumentClient.uploadDocuments("authorisation", "serviceAuthorisation",
            "CriminalInjuriesCompensation", "ST_CIC", new ArrayList<>());

        // Then
        assertEquals(response, uploadResponse);
        assertEquals(response.getDocuments(), uploadResponse.getDocuments());
    }

    @Test
    public void shouldDeleteDocument() {
        // Given
        final UUID docUUID = UUID.randomUUID();

        // When
        caseDocumentClient.deleteDocument("authorisation", "serviceAuthorisation", docUUID, true);

        // Then
        verify(caseDocumentClientApi).deleteDocument(eq("authorisation"), eq("serviceAuthorisation"),
            eq(docUUID), eq(true));
    }
}
