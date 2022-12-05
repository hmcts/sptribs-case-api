package uk.gov.hmcts.sptribs.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "uk.gov.notify.email")
@Validated
@Getter
public class EmailTemplatesConfigCIC {
    @NotNull
    private final Map<String, String> templatesCIC = new HashMap<>();

    @NotNull
    private final Map<String, String> templateVars = new HashMap<>();
}
