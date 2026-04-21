//package uk.gov.hmcts.sptribs.caseworker.event.page;
//
//import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
//import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
//import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
//import uk.gov.hmcts.ccd.sdk.type.ListValue;
//import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
//import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
//import uk.gov.hmcts.sptribs.ciccase.model.State;
//import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
//import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
//import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
//import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
//import uk.gov.hmcts.sptribs.document.persistence.Statement;
//
//import java.util.List;
//
//import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateCaseworkerCICDocumentFormat;
//
//public class UploadHearingStatements implements CcdPageConfiguration {
//
//    private static final String ALWAYS_HIDE = "newCaseworkerCICDocumentUpload=\"NEVER_SHOW\"";
//    @Override
//    public void addTo(PageBuilder pageBuilder) {
//        pageBuilder
//            .page("uploadStatement")
//            .pageLabel("Upload statement")
//            .label("uploadStatementLabel", "Upload the statement document")
//            .complex(CaseData::getStatementDocument)
//            .done();
//    }
//
////    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
////                                                                  CaseDetails<CaseData, State> detailsBefore) {
////        final CaseData data = details.getData();
////
////        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = data.getNewDocManagement().getCaseworkerCICDocumentUpload();
////        List<String> errors = validateCaseworkerCICDocumentFormat(uploadedDocuments);
////
////        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
////            .data(data)
////            .errors(errors)
////            .build();
////    }
//}
