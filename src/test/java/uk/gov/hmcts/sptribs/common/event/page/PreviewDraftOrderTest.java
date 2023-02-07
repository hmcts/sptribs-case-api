//package uk.gov.hmcts.sptribs.common.event.page;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
//import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
//import uk.gov.hmcts.ccd.sdk.api.Event;
//import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
//import uk.gov.hmcts.sptribs.ciccase.model.State;
//import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
//import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
//
//import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
//import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
//
//
//@ExtendWith(MockitoExtension.class)
//class PreviewDraftOrderTest {
//    final CaseData caseData = caseData();
//    final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
//    final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
//
//
//
//
//
//    @Test
//        public void shouldSuccessFullyPreviewDraftOrdTemplates(){
//      //  final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
//     //   PageBuilder pb = new PageBuilder();
//        PreviewDraftOrder previewOrder = new PreviewDraftOrder();
//        previewOrder.addTo(PageBuilder);
//
//    }
//
//
//
//
//
//
//}
