package uk.gov.hmcts.sptribs.systemupdate.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LenientLocalDateDeserializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void givenIsoFormattedDate_whenDeserialized_thenReturnsCorrectLocalDate() throws Exception {

        Wrapper wrapper = objectMapper.readValue("{\"date\":\"2025-02-05\"}", Wrapper.class);

        assertThat(wrapper.date).isEqualTo(LocalDate.of(2025, 2, 5));
    }

    @Test
    void givenLegacyFormattedDate_whenDeserialized_thenDoesNotThrow() {

        assertThatCode(() -> objectMapper.readValue("{\"date\":\"05 Feb 2025\"}", Wrapper.class))
            .doesNotThrowAnyException();
    }

    @Test
    void givenLegacyFormattedDate_whenDeserialized_thenFallsBackAndParsesCorrectly() throws Exception {

        Wrapper wrapper = objectMapper.readValue("{\"date\":\"05 Feb 2025\"}", Wrapper.class);

        assertThat(wrapper.date).isEqualTo(LocalDate.of(2025, 2, 5));
    }

    @Test
    void givenEmptyStringDate_whenDeserialized_thenReturnsNull() throws Exception {

        Wrapper wrapper = objectMapper.readValue("{\"date\":\"\"}", Wrapper.class);

        assertThat(wrapper.date).isNull();
    }

    @Test
    void givenNullDate_whenDeserialized_thenReturnsNull() throws Exception {

        Wrapper wrapper = objectMapper.readValue("{\"date\":null}", Wrapper.class);

        assertThat(wrapper.date).isNull();
    }

    @Test
    void givenGarbageValue_whenDeserialized_thenDoesNotThrow() {

        assertThatCode(() -> objectMapper.readValue("{\"date\":\"not a date at all\"}", Wrapper.class))
            .doesNotThrowAnyException();
    }

    @Test
    void givenGarbageValue_whenDeserialized_thenResultingValueIsNull() throws Exception {

        Wrapper wrapper = objectMapper.readValue("{\"date\":\"not a date at all\"}", Wrapper.class);

        assertThat(wrapper.date).isNull();
    }

    private static class Wrapper {
        @JsonDeserialize(using = LenientLocalDateDeserializer.class)
        public LocalDate date;
    }
}
