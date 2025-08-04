package uk.gov.hmcts.sptribs.services.cdam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Classification;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.model.DocumentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_CONTENT_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.JSON_FILE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(MockitoExtension.class)
class CaseDocumentApiServiceTest {

    @InjectMocks
    CaseDocumentApiService caseDocumentApiService;

    @Mock
    DssCaseDocumentClient caseDocumentClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Test
    void shouldUploadDocuments() throws Exception {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final MockMultipartFile multipartFile = new MockMultipartFile(
            JSON_FILE_TYPE,
                CASE_DATA_FILE_CIC,
            JSON_CONTENT_TYPE,
            caseDataJson.getBytes()
        );

        final List<MultipartFile> multipartFileList = new ArrayList<>();

        multipartFileList.add(multipartFile);

        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);

        final Document.Links links = new Document.Links();
        links.binary = new Document.DocumentLink();
        links.self = new Document.DocumentLink();
        links.binary.href = "binaryUrl";
        links.self.href = "selfURL";

        final Document document = new Document();
        document.setClassification(Classification.RESTRICTED);
        document.setOriginalDocumentName(CASE_DATA_FILE_CIC);
        document.setHashToken("SomeToken");
        document.setSize(caseDataJson.getBytes().length);
        document.setMimeType(JSON_CONTENT_TYPE);

        document.links = links;

        final List<Document> documents = new ArrayList<>();
        documents.add(document);

        final AppsConfig.AppsDetails appsDetails = new AppsConfig.AppsDetails();

        appsDetails.setJurisdiction(ST_CIC_JURISDICTION);
        appsDetails.setCaseType(ST_CIC_CASE_TYPE);
        appsDetails.setJurisdiction(ST_CIC_JURISDICTION);
        appsDetails.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
        AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent("citizen-prl-update-dss-application");
        appsDetails.setEventIds(eventsConfig);

        final UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setDocuments(documents);

        when(caseDocumentClient.uploadDocuments(
            CASE_TEST_AUTHORIZATION,
            TEST_AUTHORIZATION_TOKEN,
            ST_CIC_CASE_TYPE,
            ST_CIC_JURISDICTION,
            multipartFileList
        )).thenReturn(uploadResponse);

        final DocumentInfo testUploadResponse = caseDocumentApiService.uploadDocument(
            CASE_TEST_AUTHORIZATION,
            multipartFile,
            appsDetails
        );

        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(links.binary.toString())
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        assertNotNull(testUploadResponse);
        assertEquals(documentInfo.getFileName(), testUploadResponse.getFileName());
        assertEquals(document.links.self.href, testUploadResponse.getUrl());
    }

    @Test
    void shouldDeleteDocument() {
        final DocumentInfo documentInfo = DocumentInfo.builder()
            .documentId(UUID.randomUUID().toString())
            .url(TEST_URL)
            .fileName(CASE_DATA_FILE_CIC).build();

        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);

        caseDocumentApiService.deleteDocument(CASE_TEST_AUTHORIZATION,documentInfo.getDocumentId());

        verify(authTokenGenerator, times(1)).generate();

        verify(caseDocumentClient).deleteDocument(
            CASE_TEST_AUTHORIZATION,
            TEST_AUTHORIZATION_TOKEN,
            UUID.fromString(documentInfo.getDocumentId()),
            true
        );
    }
}
