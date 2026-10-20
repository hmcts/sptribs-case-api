package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.common.config.jackson.JacksonConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
 *
 * <p>WPAF1 has five: the appellant's date of birth and proposed discharge date, the date of the
 * decision letter, the date the declaration was signed, and the date on a row of supporting
 * evidence. The decision-letter date is the one that matters most — the whole 12-month and
 * 24-month time-limit analysis runs from it.
 */
class StWpaf1DateSerializationTest {

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
     * Every {@code LocalDate} reachable from the case data: on the case data itself, inside the
     * unwrapped complexes, and inside a collection's row type. The collection case matters as
     * much as the others — a row's date renders just as blank in the tab — and it is not reachable
     * by walking field types alone, because the row type appears only as a type argument of
     * {@code List<ListValue<T>>}.
     */
    private static List<Field> localDateFields() {
        final List<Field> dates = new ArrayList<>();
        for (Field field : StWpaf1CaseData.class.getDeclaredFields()) {
            if (field.getAnnotation(JsonUnwrapped.class) != null) {
                dates.addAll(datesOn(field.getType()));
            } else if (isCcdDate(field)) {
                dates.add(field);
            } else {
                collectionRowType(field).ifPresent(row -> dates.addAll(datesOn(row)));
            }
        }
        return dates;
    }

    private static List<Field> datesOn(final Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).filter(StWpaf1DateSerializationTest::isCcdDate).toList();
    }

    /**
     * The {@code T} of a {@code List<ListValue<T>>} field, unwrapping both layers.
     */
    private static Optional<Class<?>> collectionRowType(final Field field) {
        if (!List.class.isAssignableFrom(field.getType()) || !(field.getGenericType() instanceof ParameterizedType list)) {
            return Optional.empty();
        }
        if (!(list.getActualTypeArguments()[0] instanceof ParameterizedType listValue)) {
            return Optional.empty();
        }
        return listValue.getActualTypeArguments()[0] instanceof Class<?> row ? Optional.of(row) : Optional.empty();
    }

    private static boolean isCcdDate(final Field field) {
        return field.getType() == LocalDate.class && field.getAnnotation(JsonProperty.class) != null;
    }
}
