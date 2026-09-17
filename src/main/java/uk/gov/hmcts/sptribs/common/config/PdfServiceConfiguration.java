package uk.gov.hmcts.sptribs.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.sptribs.common.service.PdfServiceClient;

import java.util.List;
import java.util.Map;

import static java.net.URI.create;
import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class PdfServiceConfiguration {
    private static final MediaType API_VERSION =
        MediaType.valueOf("application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8");

    @Value("${pdf.api.url}")
    private String pdfApiUrl;

    @Bean
    public PdfServiceClient pdfServiceClient(
        RestTemplate restTemplate,
        ObjectMapper objectMapper
    ) {
        return (template, placeholders) -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(API_VERSION);
            headers.setAccept(List.of(MediaType.APPLICATION_PDF));

            GeneratePdfRequest request = new GeneratePdfRequest(new String(template, UTF_8), placeholders);
            try {
                return restTemplate.postForObject(
                    create(pdfApiUrl).resolve("/pdfs"),
                    new HttpEntity<>(objectMapper.writeValueAsString(request), headers),
                    byte[].class
                );
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException("Failed to convert PDF request into JSON", exception);
            }
        };
    }

    private record GeneratePdfRequest(String template, Map<String, Object> placeholders) {
    }
}
