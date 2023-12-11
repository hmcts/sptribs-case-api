//package uk.gov.hmcts.sptribs.caseworker;
//
//import  uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
//import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
//import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
//import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.http.HttpStatus.OK;
//import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
//import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
//import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;
//
//@org.springframework.boot.test.context.SpringBootTest
//public class CaseworkerCreateDraftOrderFT extends uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite {
//
//    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
//
//    @org.junit.jupiter.api.Test
//    public void shouldSuccessfullySaveDraftOrder() throws java.io.IOException {
//        // Given
//        final java.util.Map<String, Object> caseData = caseData(REQUEST);
//
//        // When
//        io.restassured.response.Response
//            response =
//            triggerCallback(caseData,
//              EventConstants.CASEWORKER_CREATE_DRAFT_ORDER, ABOUT_TO_SUBMIT_URL);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK.value());
//        // Then
//
//        assertThatJson(response.asString())
//            .when(IGNORING_EXTRA_FIELDS)
//            .isEqualTo(json(expectedResponse(
//                "classpath:responses/response-caseworker-create-draft-order-about-to-submit.json"
//            )));
//    }
//}
