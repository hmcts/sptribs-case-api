package uk.gov.hmcts.sptribs.document;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DocumentClient documentClient;

    @Test
    public void shouldGenerateDocumentBinary() {
        final byte[] documentContent = new byte[100];
        ResponseEntity<byte[]> dmStoreOKResponse = ResponseEntity.ok(documentContent);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(dmStoreOKResponse);

        final ResponseEntity<byte[]> document = documentClient.getDocumentBinary("", "", UUID.randomUUID());

        Assertions.assertThat(document.getBody()).isNotNull();

    }

    @Test
        public void shouldReturnExceptionOnBadRequest() {
        ResponseEntity<byte[]> dmStoreBadRequestResponse = ResponseEntity.badRequest().build();

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(dmStoreBadRequestResponse);

        final ResponseEntity<byte[]> document = documentClient.getDocumentBinary("", "", UUID.randomUUID());

        assertThat(document.getStatusCode(), is(HttpStatus.BAD_REQUEST));

    }

}
