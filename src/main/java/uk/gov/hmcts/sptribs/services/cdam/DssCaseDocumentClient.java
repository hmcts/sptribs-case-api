package uk.gov.hmcts.sptribs.services.cdam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;

import java.util.List;
import java.util.UUID;

@Service
public class DssCaseDocumentClient {

    private final CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    public DssCaseDocumentClient(CaseDocumentClientApi caseDocumentClientApi) {
        this.caseDocumentClientApi = caseDocumentClientApi;
    }

    public UploadResponse uploadDocuments(String authorisation, String serviceAuth, String caseTypeId,
                                          String jurisdictionId, List<MultipartFile> files) {
        return uploadDocuments(authorisation, serviceAuth, caseTypeId, jurisdictionId, files,
                               Classification.RESTRICTED);
    }

    public UploadResponse uploadDocuments(String authorisation, String serviceAuth,
                                           String caseTypeId,
                                           String jurisdictionId,
                                           List<MultipartFile> files,
                                           Classification classification) {

        final DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(classification.toString(),
                                                                                caseTypeId,
                                                                                jurisdictionId,
                                                                                files);

        return caseDocumentClientApi.uploadDocuments(authorisation, serviceAuth, documentUploadRequest);
    }

    public void deleteDocument(String authorisation, String serviceAuth, UUID documentId, boolean permanent) {
        caseDocumentClientApi.deleteDocument(authorisation, serviceAuth, documentId, permanent);
    }
}
