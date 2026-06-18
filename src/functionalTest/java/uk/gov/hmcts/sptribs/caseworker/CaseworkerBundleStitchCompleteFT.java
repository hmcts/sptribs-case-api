package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CONFIRMATION_HEADER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerBundleStitchCompleteFT extends FunctionalTestSuite {

    private static final long BUNDLE_CASE_DOCUMENT_TYPE = 6L;

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-bundle-stitch-complete-submitted.json";

    private static final String CASEWORKER_BUNDLE_STITCH_COMPLETE = "asyncStitchingComplete";

    @Test
    public void shouldSaveLatestBundleDocumentInSubmittedCallback() throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);

        final Response response = triggerCallback(
            caseData,
            CASEWORKER_BUNDLE_STITCH_COMPLETE,
            SUBMITTED_URL
        );

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Documents added successfully");

        long testCaseRef = Long.parseLong(
            caseData.get("hyphenatedCaseRef").toString().replace("-", "")
        );

        List<DocumentEntity> documentEntities =
            caseDocumentsFTDataManager.getDocumentEntities(testCaseRef);

        assertThat(documentEntities).hasSize(1);

        DocumentEntity documentEntity = documentEntities.getFirst();

        assertThat(documentEntity.getCaseReferenceNumber()).isEqualTo(testCaseRef);
        assertThat(documentEntity.getSavedAt()).isNotNull();
        assertThat(documentEntity.isDraft()).isFalse();
        assertThat(documentEntity.isSentToApplicantViaContactParties()).isFalse();
        assertThat(documentEntity.getDocumentUrl()).isNotNull();
        assertThat(documentEntity.getDocumentFilename()).isNotNull();
        assertThat(documentEntity.getDocumentBinaryUrl()).isNotNull();
        assertThat(documentEntity.getDocumentTypeName()).isNull();
        assertThat(documentEntity.getCaseDocumentTypeId()).isEqualTo(BUNDLE_CASE_DOCUMENT_TYPE);
    }


}
