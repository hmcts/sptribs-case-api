package uk.gov.hmcts.sptribs.notification;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.sptribs.common.config.ControllerConstants;

import java.util.UUID;

@Component
@Slf4j
public class ProxyClient {
    private final RestTemplate restTemplate;

    @Value("${notify_proxy.url}")
    private String baseUrl;

    @Autowired
    public ProxyClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<byte[]> getDocumentBinary(String authorisation,
                                                    String serviceAuth,
                                                    UUID documentId) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorisation);
            headers.set(ControllerConstants.SERVICE_AUTHORIZATION, serviceAuth);
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
            String apiUrl = baseUrl + "/notifications/email";
            String url = String.format(apiUrl, documentId);
            log.info(url);

            final ResponseEntity<byte[]> document = restTemplate.exchange(
                url,
                HttpMethod.GET, requestEntity,
                byte[].class
            );

            log.debug("Result of {} metadata call: {}", documentId, document);

            return document;
        } catch (HttpClientErrorException exception) {
            log.error("Exception: {}", exception.getMessage());
            throw exception;
        }
    }
}
