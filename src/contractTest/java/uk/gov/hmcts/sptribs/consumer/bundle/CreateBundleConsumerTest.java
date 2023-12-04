package uk.gov.hmcts.sptribs.consumer.bundle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CreateBundleConsumerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BundleApiClient bundleApiClient;

    private static final String ACCESS_TOKEN = "111";
    private static final String AUTHORIZATION_TOKEN = "222";
    private static final String ENDPOINT = "/api/new-bundle";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
 //   private static BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder().build();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bundleApiClient = new BundleApiClient(restTemplate);
    }



    @Test
    public void testCreateEvidenceBundleId() {
        // Given
        String expectedUrl = "http://localhost:8080" + ENDPOINT;
        HttpHeaders expectedHeaders = createExpectedHeaders();

        // Mocking the behavior of RestTemplate
        when(restTemplate.postForObject(expectedUrl, null, BundleCreateResponse.class, expectedHeaders))
            .thenReturn(new BundleCreateResponse("12345"));

        // When
        BundleCreateResponse bundleCreateResponse = bundleApiClient.createEvidenceBundle();

        // Then
//        assertThat(bundleCreateResponse).isNotNull();
//        assertThat(bundleCreateResponse.getId()).isEqualTo("12345");

        assertThat(bundleCreateResponse).isNotNull();
      //  assertThat(bundleCreateResponse.getId()).isEqualTo("12345");

        // Additional assertions based on your specific response structure
        assertThat(bundleCreateResponse.getCaseBundles()).hasSize(1);

        CaseBundle firstCaseBundle = bundleCreateResponse.getCaseBundles().get(0);
        assertThat(firstCaseBundle.getFolders()).hasSize(4);
        assertThat(firstCaseBundle.getStitchStatus()).isEqualTo("DONE");
        assertThat(firstCaseBundle.getStitchedDocument().getDocumentFilename()).isEqualTo("StitchedPDF");
    }
    @Test
    public void testCreateEvidenceBundleWithRestTemplate() {
        String expectedUrl = "http://localhost:8080" + ENDPOINT;
        HttpHeaders expectedHeaders = createExpectedHeaders();
        when(restTemplate.postForObject(expectedUrl, null, BundleCreateResponse.class, expectedHeaders))
            .thenReturn(new BundleCreateResponse("12345"));

        // When
        BundleCreateResponse bundleCreateResponse = bundleApiClient.createEvidenceBundle();
        // When
       // BundleCreateResponse bundleCreateResponse = bundleApiClient.createEvidenceBundle();
//        BundleCreateResponse bundleCreateResponse = bundleApiClient.createBundleServiceRequvest(BEARER_TOKEN,
//            SERVICE_AUTHORIZATION_HEADER, BundleCreateRequest.builder().build()
//        );
        // Then
        assertThat(bundleCreateResponse).isNotNull();
        assertThat(bundleCreateResponse.getId()).isEqualTo("12345");

        // Additional assertions based on your specific response structure
        assertThat(bundleCreateResponse.getCaseBundles()).hasSize(1);

        CaseBundle firstCaseBundle = bundleCreateResponse.getCaseBundles().get(0);
        assertThat(firstCaseBundle.getFolders()).hasSize(4);
        assertThat(firstCaseBundle.getStitchStatus()).isEqualTo("DONE");
        assertThat(firstCaseBundle.getStitchedDocument().getDocumentFilename()).isEqualTo("StitchedPDF");
    }


    private static HttpHeaders createExpectedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER);
        return headers;
    }

    // ... Other test methods or classes



    private static class BundleApiClient {

        private final RestTemplate restTemplate;

        public BundleApiClient(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public BundleCreateResponse createEvidenceBundle() {
            HttpHeaders headers = createExpectedHeaders();
            return restTemplate.postForObject("http://localhost:8080" + ENDPOINT, null, BundleCreateResponse.class, headers);
        }
    }
}
