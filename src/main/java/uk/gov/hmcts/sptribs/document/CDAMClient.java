package uk.gov.hmcts.sptribs.document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

public class CDAMClient {
    RestTemplate restTemplate;

    @Bean
    private RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    public Object getDocument(String serviceAuthorizationToken,
                             String authorization,
                             @Value("${${case_document_am.url}/cases/documents{documentId}/binary}") String url, String documentId) {


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, authorization);

        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        Object response=null;

        try {
            response =
                restTemplate
                    .exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity
                        , Object.class, documentId
                    ).getBody();

        } catch (RestClientResponseException e) {
        }
        return response;
    }
}
