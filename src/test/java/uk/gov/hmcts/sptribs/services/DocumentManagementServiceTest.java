package uk.gov.hmcts.sptribs.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.exception.DocumentUploadOrDeleteException;
import uk.gov.hmcts.sptribs.model.DocumentInfo;
import uk.gov.hmcts.sptribs.model.DocumentResponse;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentApiService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_DELETE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.DOCUMENT_UPLOAD_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_FILE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.RESPONSE_STATUS_SUCCESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
class DocumentManagementServiceTest {

    @InjectMocks
    private DocumentManagementService documentManagementService;

    @Mock
    private CaseDocumentApiService caseDocumentApiService;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AppsConfig.AppsDetails fgmAppDetail;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUploadFgmDocument() throws Exception {
        fgmAppDetail = new AppsConfig.AppsDetails();
        fgmAppDetail.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        fgmAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        fgmAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
        AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent("");

        fgmAppDetail.setEventIds(eventsConfig);

        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        when(appsConfig.getApps()).thenReturn(List.of(fgmAppDetail));

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final MockMultipartFile multipartFile = new MockMultipartFile(
            JSON_FILE_TYPE,
                CASE_DATA_FILE_CIC,
            JSON_CONTENT_TYPE,
            caseDataJson.getBytes()
        );

        when(caseDocumentApiService.uploadDocument(
            CASE_TEST_AUTHORIZATION,
            multipartFile,
            fgmAppDetail
        )).thenReturn(documentInfo);

        DocumentResponse testUploadResponse = documentManagementService.uploadDocument(
            CASE_TEST_AUTHORIZATION,
            fgmAppDetail
                .getCaseTypeOfApplication()
                .stream()
                .filter(eachCase -> eachCase.equals(CASE_DATA_CIC_ID))
                .findFirst()
                .orElse(null),
            multipartFile
        );


        assertNotNull(fgmAppDetail);
        assertNotNull(testUploadResponse);
        assertEquals(documentInfo.getDocumentId(), testUploadResponse.getDocument().getDocumentId());
        assertEquals(documentInfo.getFileName(), testUploadResponse.getDocument().getFileName());
        assertEquals(documentInfo.getUrl(), testUploadResponse.getDocument().getUrl());
        assertEquals(RESPONSE_STATUS_SUCCESS, testUploadResponse.getStatus());
    }

    @Test
    void shouldUploadFgmDocumentFailedWithException() throws Exception {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final MockMultipartFile multipartFile = new MockMultipartFile(
            JSON_FILE_TYPE,
                CASE_DATA_FILE_CIC,
            JSON_CONTENT_TYPE,
            caseDataJson.getBytes()
        );

        when(caseDocumentApiService.uploadDocument(
            CASE_TEST_AUTHORIZATION,
            multipartFile,
            fgmAppDetail
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_UPLOAD_FAILURE_MSG,
                new RuntimeException()
            ));

        final DocumentUploadOrDeleteException documentUploadOrDeleteException =
            assertThrows(DocumentUploadOrDeleteException.class, () ->
            documentManagementService.uploadDocument(CASE_TEST_AUTHORIZATION, CASE_DATA_CIC_ID, multipartFile));

        assertNotNull(documentUploadOrDeleteException.getCause());
        assertTrue(documentUploadOrDeleteException.getCause() instanceof RuntimeException);
        assertTrue(documentUploadOrDeleteException.getMessage().contains(DOCUMENT_UPLOAD_FAILURE_MSG));
    }

    @Test
    void shouldDeleteDocument() {

        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        doNothing().when(
            caseDocumentApiService).deleteDocument(
                CASE_TEST_AUTHORIZATION,documentInfo.getDocumentId());

        final DocumentResponse testDeleteResponse = documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,documentInfo.getDocumentId());

        assertNotNull(testDeleteResponse);
        assertEquals("Success",testDeleteResponse.getStatus());

        verify(caseDocumentApiService, times(1))
            .deleteDocument(CASE_TEST_AUTHORIZATION, documentInfo.getDocumentId());
    }

    @Test
    void shouldDeleteFgmDocumentFailedWithException() {
        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(CASE_DATA_CIC_ID)
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        final String docInfoId = documentInfo.getDocumentId();

        when(documentManagementService.deleteDocument(
            CASE_TEST_AUTHORIZATION,
            documentInfo.getDocumentId()
        )).thenThrow(
            new DocumentUploadOrDeleteException(
                DOCUMENT_DELETE_FAILURE_MSG,
                new RuntimeException()
            ));

        final DocumentUploadOrDeleteException documentUploadOrDeleteException =
            assertThrows(DocumentUploadOrDeleteException.class, () ->
            documentManagementService.deleteDocument(CASE_TEST_AUTHORIZATION, docInfoId));

        assertNotNull(documentUploadOrDeleteException.getCause());
        assertTrue(documentUploadOrDeleteException.getCause() instanceof RuntimeException);
        assertTrue(documentUploadOrDeleteException.getMessage().contains(DOCUMENT_DELETE_FAILURE_MSG));
    }
}
