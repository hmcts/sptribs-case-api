package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.common.config.jackson.JacksonConfiguration;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every {@link LocalDate} in the case data serialises as a {@code yyyy-MM-dd} string.
 *
 * <p>The application's mapper registers {@code LocalDateSerializer} without disabling Jackson's
 * {@code WRITE_DATES_AS_TIMESTAMPS}, so an unannotated {@code LocalDate} is written as a JSON
 * array — {@code [2012,3,7]}. CCD's Date type cannot read that, and the consequence is invisible
 * from this service: the case is created, the field is stored, every other value is correct, and
 * the date simply renders blank in the caseworker's tab. It reads as a tab-configuration problem
 * rather than a serialisation one.
 *
 * <p>{@code @JsonFormat(pattern = "yyyy-MM-dd")} on the field overrides that, which is why every
 * CIC {@code LocalDate} carries it. This asserts the resulting JSON rather than the presence of
 * the annotation, so it stays true if the mapper is ever fixed centrally instead.
 */
class StSend35DateSerializationTest {

    private final ObjectMapper mapper = new JacksonConfiguration().getMapper();

    @Test
    void shouldWriteEveryDateAsAnIsoStringRatherThanAnArray() throws Exception {
        final List<Field> dates = localDateFields();

        assertThat(dates)
            .describedAs("no LocalDate fields found, so this test is not checking anything")
            .isNotEmpty();

        final List<String> wrong = new ArrayList<>();
        for (Field field : dates) {
            final Object holder = field.getDeclaringClass().getDeclaredConstructor().newInstance();
            field.setAccessible(true);
            field.set(holder, LocalDate.of(2012, 3, 7));

            final String json = mapper.writeValueAsString(holder);
            final String id = field.getDeclaringClass().getSimpleName() + "." + field.getName();
            if (!json.contains("\"2012-03-07\"")) {
                wrong.add(id + " -> " + json);
            }
        }

        assertThat(wrong)
            .describedAs("these dates are not written as yyyy-MM-dd strings, so CCD stores a "
                + "value it cannot render and the caseworker sees an empty field; add "
                + "@JsonFormat(pattern = \"yyyy-MM-dd\") to each")
            .isEmpty();
    }

    @Test
    void shouldAnnotateEveryDateField() {
        final List<String> unannotated = localDateFields().stream()
            .filter(field -> field.getAnnotation(JsonFormat.class) == null)
            .map(field -> field.getDeclaringClass().getSimpleName() + "." + field.getName())
            .toList();

        assertThat(unannotated)
            .describedAs("a LocalDate without @JsonFormat is serialised as an array by this "
                + "service's mapper")
            .isEmpty();
    }

    /**
     * Every {@code LocalDate} reachable from the case data, including inside the unwrapped
     * complexes.
     */
    private static List<Field> localDateFields() {
        final List<Field> dates = new ArrayList<>();
        for (Field field : StSend35CaseData.class.getDeclaredFields()) {
            if (field.getAnnotation(JsonUnwrapped.class) != null) {
                for (Field nested : field.getType().getDeclaredFields()) {
                    if (isCcdDate(nested)) {
                        dates.add(nested);
                    }
                }
            } else if (isCcdDate(field)) {
                dates.add(field);
            }
        }
        return dates;
    }

    private static boolean isCcdDate(final Field field) {
        return field.getType() == LocalDate.class && field.getAnnotation(JsonProperty.class) != null;
    }
}
