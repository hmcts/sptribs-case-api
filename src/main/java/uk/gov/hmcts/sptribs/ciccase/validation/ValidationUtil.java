package uk.gov.hmcts.sptribs.ciccase.validation;

import java.util.List;

import static java.util.Collections.emptyList;

public final class ValidationUtil {

    public static final String EMPTY = " cannot be empty or null";

    private ValidationUtil() {
    }

    public static List<String> notNull(Object value, String field) {
        return value == null ? List.of(field + EMPTY) : emptyList();
    }

}
