package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.systemupdate.service.MigrationDocumentService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.prepareDocTypeAndDocMap;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.BUNDLE;

@RequiredArgsConstructor
@Component
@Slf4j
public class SystemMigrateCaseDocumentsToDocTable implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE = "migrate-to-document-table";
    private final MigrationDocumentService documentsService;

    final Map<String, String> failedDocs = new HashMap<>();

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE)
            .forAllStates()
            .name("System: Migrate Document Table")
            .description("Migrate documents to document table")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE, SUPER_USER);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {



        CaseData caseData = caseDetails.getData();
        Long reference = caseDetails.getId();

        long startTime = System.currentTimeMillis();
        log.info("{} task started for reference={} at time {} in ms", SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, reference, startTime);

        Map<CaseDocumentType, List<CaseworkerCICDocument>> documentTypeDocumentMap = prepareDocTypeAndDocMap(caseData);

        log.info("Found {} docs for case reference: {} ", documentTypeDocumentMap.size(), reference);

        saveDocumentsToDocTable(documentTypeDocumentMap, reference);

        if (caseData.getCaseBundles() != null) {
            saveBundlesToDocTable(caseData, reference);
        }

        if (!failedDocs.isEmpty()) {
            log.error("Following documents failed to save: {}", failedDocs.keySet());
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("{} task completed for reference={} in {} ms",
            SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE,
            reference,
            duration);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void saveBundlesToDocTable(CaseData caseData, Long reference) {
        caseData.getCaseBundles().forEach(bundle ->
            saveDocument(
                bundle.getValue().getStitchedDocument(),
                reference,
                null,
                BUNDLE,
                bundle.getValue().getDateAndTime()
            )
        );
    }

    private void saveDocumentsToDocTable(
        Map<CaseDocumentType, List<CaseworkerCICDocument>> documentTypeDocumentMap,
        Long reference
    ) {
        documentTypeDocumentMap.forEach((caseDocumentType, documents) ->
            documents.forEach(document ->
                saveDocument(
                    document.getDocumentLink(),
                    reference,
                    document.getDocumentCategory(),
                    caseDocumentType,
                    document.getDate() != null
                        ? document.getDate().atTime(0, 0)
                        : null
                )
            )
        );
    }

    private void saveDocument(Document document, Long reference, DocumentType documentType, CaseDocumentType caseDocumentType,
                              LocalDateTime uploadedTime
    ) {
        if (document == null) {
            return;
        }

        String binaryUrl = document.getBinaryUrl();

        try {
            documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(
                document,
                reference,
                documentType,
                caseDocumentType,
                uploadedTime
            );
        } catch (DocumentSaveException e) {
            failedDocs.put(binaryUrl, e.getMessage());
            log.info("Failed to save document {} to table: {}", binaryUrl, e.getMessage());
        }
    }
}
