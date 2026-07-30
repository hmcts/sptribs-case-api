package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.service.CorrespondenceDocumentService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactPartiesService {

    private final DocumentsService documentsService;
    private final CorrespondenceDocumentService correspondenceDocumentService;

    public void linkCorrespondenceIdsToDocuments(CaseData caseData,
                                                 List<String> correspondenceIds) {
        try {
            List<CaseworkerCICDocument> sentDocuments =
                DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData
                    .getContactPartiesDocuments()
                    .getDocumentList());

            List<Long> documentIds = sentDocuments
                .stream()
                .map(DocumentUtil::getDocumentId)
                .map(Long::valueOf)
                .toList();
//            List<Long> documentIds =
//                documentsService.getDocumentsViaSentByContactParties(caseData, uploadedDocuments);

            for (String correspondenceId : correspondenceIds) {
                try {
                    correspondenceDocumentService.saveCorrespondenceDocumentLink(correspondenceId, documentIds);
                } catch (CorrespondenceDocumentSaveException e) {
                    log.error(
                        "Unable to link documents for correspondenceId {}. Continuing with remaining correspondences.",
                        correspondenceId,
                        e
                    );
                }
            }
        } catch (DocumentLookupException e) {
            log.error(
                "Notifications were sent successfully, but document IDs could not be retrieved. "
                    + "Skipping correspondence document linking.",
                e
            );
        }
    }
}
