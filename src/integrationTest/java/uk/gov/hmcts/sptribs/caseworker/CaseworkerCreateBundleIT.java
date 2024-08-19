package uk.gov.hmcts.sptribs.caseworker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.bundling.client.BundleResponse;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingClient;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocument;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CaseworkerCreateBundleIT {

    private static final String CASEWORKER_CREATE_BUNDLE_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:caseworker-create-bundle-about-to-submit-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private BundlingClient bundlingClient;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldMakeCallToEvidenceManagementToCreateBundleInAboutToSubmit() throws Exception {
        final CaseData caseData = caseData();
        populateCaseDocuments(caseData);
        final BundleResponse bundleResponse = mock(BundleResponse.class);

        when(bundleResponse.getData()).thenReturn(new LinkedHashMap<>());
        when(bundlingClient.createBundle(
            eq(SERVICE_AUTHORIZATION),
            eq(TEST_AUTHORIZATION_TOKEN),
            any(BundleCallback.class)
        )).thenReturn(bundleResponse);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CREATE_BUNDLE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CREATE_BUNDLE_ABOUT_TO_SUBMIT_RESPONSE)));

        verify(bundlingClient)
            .createBundle(
                eq(SERVICE_AUTHORIZATION),
                eq(TEST_AUTHORIZATION_TOKEN),
                any(BundleCallback.class)
            );
    }

    private void populateCaseDocuments(final CaseData caseData) {
        final Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        final ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        caseData.getCicCase().setOrderList(List.of(orderListValue));
        caseData.getCicCase().setApplicantDocumentsUploaded(
            List.of(
                getCaseworkerCICDocument("file1.pdf"),
                getCaseworkerCICDocument("test.pdf"),
                getCaseworkerCICDocument("evidence.pdf")
            )
        );
        caseData.getCicCase().setReinstateDocuments(
            List.of(
                getCaseworkerCICDocument("reinstate_doc.pdf")
            )
        );
    }
}
