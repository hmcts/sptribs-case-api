package uk.gov.hmcts.sptribs.document.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesAllowedFileTypes;
import uk.gov.hmcts.sptribs.document.DocumentFileTypes;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentReadPurpose;

import java.util.EnumSet;
import java.util.Set;

@Component
public class CaseDocumentPolicy {

    private static final Set<CaseDocumentType> CASE_VIEW_TYPES = EnumSet.of(
        CaseDocumentType.APPLICATION,
        CaseDocumentType.DOCUMENT_MANAGEMENT,
        CaseDocumentType.ORDER,
        CaseDocumentType.DRAFT_ORDER,
        CaseDocumentType.DECISION,
        CaseDocumentType.FINAL_DECISION,
        CaseDocumentType.HEARING_RECORD,
        CaseDocumentType.OTHER
    );

    private static final Set<CaseDocumentType> ACTIONABLE_TYPES = EnumSet.of(
        CaseDocumentType.APPLICATION,
        CaseDocumentType.DOCUMENT_MANAGEMENT,
        CaseDocumentType.ORDER,
        CaseDocumentType.DECISION,
        CaseDocumentType.FINAL_DECISION,
        CaseDocumentType.HEARING_RECORD,
        CaseDocumentType.OTHER
    );

    public boolean includes(DocumentReadPurpose purpose, CaseDocumentType type, DocumentEntity document) {
        if (document == null || type == null || StringUtils.isBlank(document.getDocumentUrl())) {
            return false;
        }

        return switch (purpose) {
            case CASE_VIEW -> CASE_VIEW_TYPES.contains(type);
            case BUNDLE -> ACTIONABLE_TYPES.contains(type)
                && DocumentFileTypes.isValid(
                    document.getDocumentFilename(),
                    DocumentFileTypes.BUNDLE_DOCUMENT_EXTENSIONS
                );
            case CONTACT_PARTIES -> ACTIONABLE_TYPES.contains(type)
                && ContactPartiesAllowedFileTypes.isFileTypeValid(
                    StringUtils.substringAfterLast(document.getDocumentFilename(), ".")
                );
            default -> false;
        };
    }
}
