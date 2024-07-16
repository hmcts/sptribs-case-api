package uk.gov.hmcts.sptribs.common.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.MapperFeature.INFER_BUILDER_TYPE_BINDINGS;
import static com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer.INSTANCE;

@Configuration
public class JacksonConfiguration {

    @Primary
    @Bean
    public ObjectMapper getMapper() {
        ObjectMapper mapper = JsonMapper.builder()
            .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .enable(INFER_BUILDER_TYPE_BINDINGS)
            .serializationInclusion(NON_NULL)
            .build();

        SimpleModule deserialization = new SimpleModule();
        deserialization.addDeserializer(HasRole.class, new HasRoleDeserializer());
        deserialization.addDeserializer(InternalHealth.class, new InternalHealthDeserializer());
        mapper.registerModule(deserialization);

        JavaTimeModule datetime = new JavaTimeModule();
        datetime.addSerializer(INSTANCE);
        mapper.registerModule(datetime);
        mapper.registerModule(new ParameterNamesModule(PROPERTIES));
        mapper.enable(INCLUDE_SOURCE_IN_LOCATION);

        return mapper;
    }
}
