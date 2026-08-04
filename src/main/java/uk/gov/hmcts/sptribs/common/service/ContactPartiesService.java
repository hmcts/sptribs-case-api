package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
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
        if (CollectionUtils.isEmpty(correspondenceIds)) {
            return;
        }

        List<Long> documentIds;

        try {
            documentIds = resolveDocumentIds(caseData);
        } catch (DocumentLookupException documentLookupException) {
            log.error(
                "Notifications were sent successfully, but document IDs could not be retrieved. "
                    + "Skipping correspondence document linking.",
                documentLookupException
            );
            return;
        }

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
    }

    private List<Long> resolveDocumentIds(CaseData caseData) {
        List<String> documentUUIDs =
            DocumentListUtil
                .getSelectedDocumentsFromDynamicList(caseData, caseData
                    .getContactPartiesDocuments()
                    .getDocumentList())
                .stream()
                .map(DocumentUtil::getDocumentUUID)
                .toList();

        return documentsService.getDocumentIdsByDocumentBinaryUrls(documentUUIDs);

    }
}
