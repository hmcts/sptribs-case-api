package uk.gov.hmcts.sptribs.services.cdam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.model.DocumentInfo;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CaseDocumentApiService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    DssCaseDocumentClient dssCaseDocumentClient;

    private static final Logger logger = LoggerFactory.getLogger(CaseDocumentApiService.class);

    public DocumentInfo uploadDocument(String authorizationToken, MultipartFile file,
                                       AppsConfig.AppsDetails appsDetails) {

        final String serviceAuthToken = authTokenGenerator.generate();

        logger.info("serviceAuthToken: " + serviceAuthToken);

        final UploadResponse uploadResponse = dssCaseDocumentClient.uploadDocuments(
            authorizationToken,
            serviceAuthToken,
            appsDetails.getCaseType(),
            appsDetails.getJurisdiction(),
            List.of(file)
        );

        logger.info("uploadResponse: " + uploadResponse.toString());

        final Document uploadedDocument = uploadResponse.getDocuments().get(0);

        logger.info("uploadedDocument: " + uploadedDocument.toString());

        final String[] split = uploadedDocument.links.self.href.split("/");

        logger.info("split: " + Arrays.toString(split));

        return DocumentInfo.builder()
            .url(uploadedDocument.links.self.href)
            .binaryUrl(uploadedDocument.links.binary.href)
            .fileName(uploadedDocument.originalDocumentName)
            .documentId(split[split.length - 1])
            .build();
    }

    public void deleteDocument(String authorizationToken, String documentId) {
        dssCaseDocumentClient.deleteDocument(
            authorizationToken,
            authTokenGenerator.generate(),
            UUID.fromString(documentId),
            true
        );
    }
}

