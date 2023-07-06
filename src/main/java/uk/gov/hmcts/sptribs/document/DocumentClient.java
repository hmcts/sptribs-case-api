package uk.gov.hmcts.sptribs.document;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.sptribs.common.config.ControllerConstants;

import java.util.UUID;

@Component
@Slf4j
public class DocumentClient {
    private final RestTemplate restTemplate;

    @Value("${case_document_am.url}")
    private String url;

    @Autowired
    public DocumentClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // @GetMapping(value = "/{documentId}/binary")
    public ResponseEntity<byte[]> getDocumentBinary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                    @RequestHeader(ControllerConstants.SERVICE_AUTHORIZATION) String serviceAuth,
                                                    @PathVariable(DocumentConstants.DOCUMENT_ID) UUID documentId) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorisation);
            headers.set(ControllerConstants.SERVICE_AUTHORIZATION, serviceAuth);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            String apiUrl = url + "/cases/documents/%s/binary";
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
