package uk.gov.hmcts.sptribs.systemupdate.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Slf4j
public class LenientLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LEGACY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.UK);

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value, ISO_FORMAT);
        } catch (DateTimeParseException isoFailure) {
            try {
                return LocalDate.parse(value, LEGACY_FORMAT);
            } catch (DateTimeParseException legacyFailure) {
                log.warn("Could not parse firstHearingDate value '{}', defaulting to null", value);
                return null;
            }
        }
    }
}
