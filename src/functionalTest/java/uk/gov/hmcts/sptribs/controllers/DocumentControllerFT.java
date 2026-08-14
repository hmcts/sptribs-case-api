package uk.gov.hmcts.sptribs.controllers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;

@SpringBootTest
public class DocumentControllerFT extends FunctionalTestSuite {

    private static final String CONTACT_PARTIES_SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-contact-parties-submitted.json";

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnDashboardDocumentsWhenDocumentsAreRetrievedByCCDReference() throws SQLException, IOException {
        // Run contact parties event to create correspondence documents
        Map<String, Object> caseData  = caseData(CONTACT_PARTIES_SUBMITTED_REQUEST);
        triggerCallback(caseData, CASEWORKER_CONTACT_PARTIES, "/callbacks/submitted", true);

        // Run createAndSendOrder and IssueDecision and IssueFinalDecision to create order/decision documents

        // Run createCaseBundle to create bundle document

        // Run getDocumentsByCCDReference
        final Long appealIdForAppealThatExists =
            Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));
        functionalTestDataManager.addReference(appealIdForAppealThatExists);

        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .header("X-Postcode", "SW1A 1AA")
            .when()
            .get("/cases/CIC/" + appealIdForAppealThatExists + "/documents");

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.getBody().asString())
            .inPath("$.documentResponse.contactPartiesDocuments").isArray()
            .hasSize(1);

        Map<String, Object> contactPartiesDocsDynamicList = (Map<String, Object>) caseData.get("contactPartiesDocumentsDocumentList");
        Map<String, Object> dynamicListObj = ((ArrayList<Map<String, Object>>) contactPartiesDocsDynamicList.get("value")).getFirst();
        String contactPartiesDocumentId = dynamicListObj.get("label").toString().split("/documents/")[1].split("/binary")[0];

//        Map<String, Object> contactPartiesDocsDynamicList = (Map<String, Object>) caseData.get("orderdocs");
//        Map<String, Object> dynamicListObj = ((ArrayList<Map<String, Object>>) contactPartiesDocsDynamicList.get("value")).getFirst();
//        String contactPartiesDocumentId = dynamicListObj.get("label").toString().split("/documents/")[1].split("/binary")[0];
//
//        Map<String, Object> contactPartiesDocsDynamicList = (Map<String, Object>) caseData.get("bundles");
//        Map<String, Object> dynamicListObj = ((ArrayList<Map<String, Object>>) contactPartiesDocsDynamicList.get("value")).getFirst();
//        String contactPartiesDocumentId = dynamicListObj.get("label").toString().split("/documents/")[1].split("/binary")[0];

        assertThatJson(response.getBody().asString())
            .inPath("$.documentResponse.contactPartiesDocuments").isArray().hasSize(1);
        assertThatJson(response.getBody().asString())
            .inPath("$.documentResponse.contactPartiesDocuments[0].document.documentLink.document_url")
            .isEqualTo("http://dm-store.service.core-compute.internal/documents/" + contactPartiesDocumentId + '"');
//        assertThatJson(response.getBody().asString())
//            .inPath("$.documentResponse.orderAndDecisionDocuments").isArray().hasSize(1);
//        assertThatJson(response.getBody().asString())
//            .inPath("$.documentResponse.latestCaseBundleDocuments").isArray().hasSize(1);
    }

//    @Test
//    public void shouldReturnForbiddenWhenCaseExistsAndUserDoesNotHaveAccess() {
//        final Long caseId = createCaseInCcd(false).getId();
//        functionalTestDataManager.addReference(caseId);
//
//        final Response response = RestAssured
//            .given()
//            .relaxedHTTPSValidation()
//            .baseUri(testUrl)
//            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
//            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
//            .when()
//            .get("/cases/cica/" + caseId + "/access");
//
//        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN.value());
//    }
}
