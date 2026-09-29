package uk.gov.hmcts.sptribs.services.cdam;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FeignSupportConfigTest {

    private final FeignSupportConfig config = new FeignSupportConfig();

    @Mock
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Test
    void shouldCreateMultipartFormEncoder() {
        Encoder encoder = config.multipartFormEncoder(messageConverters);
        assertThat(encoder).isInstanceOf(SpringFormEncoder.class);
    }

    @Test
    void shouldConfigureFeignLoggerLevel() {
        Logger.Level level = config.feignLoggerLevel();
        assertThat(level).isEqualTo(Logger.Level.FULL);
    }

    @Test
    void shouldConfigureRequestOptionsWithCorrectTimeouts() {
        Request.Options options = config.requestOptions();
        assertThat(options.connectTimeoutMillis()).isEqualTo(30000);
        assertThat(options.readTimeoutMillis()).isEqualTo(60000);
        assertThat(options.isFollowRedirects()).isTrue();
    }

    @Test
    void shouldConfigureDefaultRetryer() {
        Retryer retryer = config.retryer();
        assertThat(retryer).isNotNull();
        assertThat(retryer).isInstanceOf(Retryer.Default.class);
    }
}
