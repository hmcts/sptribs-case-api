package uk.gov.hmcts.sptribs.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.net.URI.create;

@Configuration
public class PdfServiceConfiguration {
    @Value("${pdf.api.url}")
    private String pdfApiUrl;

    @Bean
    public PDFServiceClient pdfServiceClient(
        RestTemplate restTemplate,
        ObjectMapper objectMapper
    ) {
        return PDFServiceClient.builder()
            .restOperations(restTemplate)
            .objectMapper(objectMapper)
            .build(create(pdfApiUrl));
    }
}
