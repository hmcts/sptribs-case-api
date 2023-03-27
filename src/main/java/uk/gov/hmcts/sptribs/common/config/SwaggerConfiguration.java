package uk.gov.hmcts.sptribs.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("Sptribs case API")
                .description("Callback handler for CCD")
                .version("v0.0.1"));
    }
}
