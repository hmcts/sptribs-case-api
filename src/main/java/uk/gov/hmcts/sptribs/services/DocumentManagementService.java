package uk.gov.hmcts.sptribs.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.exception.DocumentUploadOrDeleteException;
import uk.gov.hmcts.sptribs.model.DocumentInfo;
import uk.gov.hmcts.sptribs.model.DocumentResponse;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentApiService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

@Service
@Slf4j
public class DocumentManagementService {

    @Autowired
    CaseDocumentApiService caseDocumentApiService;

    @Autowired
    AppsConfig appsConfig;

    public DocumentResponse uploadDocument(String authorization, String caseTypeOfApplication, MultipartFile file) {
        try {
            final DocumentInfo document = caseDocumentApiService.uploadDocument(authorization, file, AppsUtil
                .getExactAppsDetails(appsConfig, caseTypeOfApplication));
            return DocumentResponse.builder().status("Success").document(document).build();

        } catch (Exception e) {
            log.error("Error while uploading document. " + e.getMessage());
            throw new DocumentUploadOrDeleteException("Failing while uploading the document. The error message is "
                                                           + e.getMessage(), e);
        }
    }

    public DocumentResponse deleteDocument(String authorization, String documentId) {
        try {
            caseDocumentApiService.deleteDocument(authorization, documentId);
            return DocumentResponse.builder().status("Success").build();

        } catch (Exception e) {
            log.error("Error while deleting  document. " + e.getMessage());
            throw new DocumentUploadOrDeleteException("Failing while deleting the document for Id: "
                + documentId + " The error message is "
                + e.getMessage(), e);
        }
    }
}
