package uk.gov.hmcts.sptribs.bankholidays.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Configuration
public class BankHolidayConfiguration {

    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
        ObjectProvider<ClientHttpMessageConvertersCustomizer> clientCustomizers,
        ObjectProvider<HttpMessageConverterCustomizer> feignCustomizers
    ) {
        return new FeignHttpMessageConverters(clientCustomizers, feignCustomizers);
    }

    @Bean
    public Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
    }

    @Bean
    @SuppressWarnings("removal")
    public HttpMessageConverterCustomizer bankHolidayHttpMessageConverterCustomizer(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jacksonConverter =
            new MappingJackson2HttpMessageConverter(objectMapper);
        jacksonConverter.setSupportedMediaTypes(List.of(
            APPLICATION_JSON,
            new MediaType("application", "*+json"),
            TEXT_PLAIN
        ));
        return converters -> converters.addFirst(jacksonConverter);
    }

    @Bean
    public Encoder feignEncoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new SpringEncoder(messageConverters);
    }
}
