package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.prepareDocTypeAndDocMap;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.BUNDLE;

@RequiredArgsConstructor
@Component
public class SystemMigrateCaseDocumentsToDocTable implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE = "migrate-to-document-table";
    private final DocumentsService documentsService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE)
            .forAllStates()
            .name("System: Migrate Document Table")
            .description("Migrate documents to document table")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = caseDetails.getData();
        Long reference = caseDetails.getId();


        Map<CaseDocumentType, List<CaseworkerCICDocument>> documentTypeDocumentMap =  prepareDocTypeAndDocMap(caseData);

        documentTypeDocumentMap.forEach((caseDocumentType, caseworkerCICDocuments) ->
            caseworkerCICDocuments.forEach(caseworkerCICDocument -> documentsService.buildAndSaveNewDocumentEntity(
                caseworkerCICDocument.getDocumentLink(),
                reference,
                caseworkerCICDocument.getDocumentCategory(),
                caseDocumentType
            )));


        //BUNDLE LOGIC - COMMENTED OUT FOR LOCAL

        if (caseData.getCaseBundles() != null) {
            caseData.getCaseBundles().stream()
                .map(bundleListValue -> bundleListValue.getValue().getStitchedDocument())
                .forEach(document -> documentsService.buildAndSaveNewDocumentEntity(
                    document,
                    reference,
                    null,
                    BUNDLE
                ));
        }


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
