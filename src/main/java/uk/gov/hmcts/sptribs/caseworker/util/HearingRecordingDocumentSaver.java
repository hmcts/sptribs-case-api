package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.handleDocumentException;

public final class HearingRecordingDocumentSaver {

    private HearingRecordingDocumentSaver() {
    }

    public static void save(Long caseId,
                            List<ListValue<CaseworkerCICDocument>> documents,
                            DocumentsService documentsService,
                            List<String> errors) {
        if (documents == null) {
            return;
        }

        for (ListValue<CaseworkerCICDocument> document : documents) {
            try {
                documentsService.buildAndSaveNewDocumentEntity(
                    document.getValue().getDocumentLink(),
                    caseId,
                    document.getValue().getDocumentCategory(),
                    CaseDocumentType.HEARING_RECORD
                );
            } catch (RuntimeException e) {
                errors.add(handleDocumentException(document.getValue().getDocumentLink(), e.getMessage()));
            }
        }
    }
}
