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

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocuments;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@RequiredArgsConstructor
@Component
public class SystemMigrateCaseDocumentsToDocTable implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE = "migrate-to-document-table";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE)
            .forAllStates()
            .name("System: Migrate Documents to Document Table")
            .description("Migrate ")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = caseDetails.getData();
        Long reference = caseDetails.getId();

        System.out.println("docs in case " + reference);
        getAllCaseDocuments(caseData).stream().forEach(doc ->
            System.out.println(doc.getValue().getDocumentLink().getFilename()));

        //Get all documents from casedata

        //assign document reference

        //save and create entity

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
