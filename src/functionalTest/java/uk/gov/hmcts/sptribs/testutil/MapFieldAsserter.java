package uk.gov.hmcts.sptribs.testutil;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Component
@SuppressWarnings("unchecked")
public class MapFieldAsserter {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private final MapValueExpander mapValueExpander;

    private MapFieldAsserter(MapValueExpander mapValueExpander) {
        this.mapValueExpander = mapValueExpander;
    }

    public void assertFields(
        Map<String, Object> expectedMap,
        Map<String, Object> actualMap,
        final String path
    ) {
        for (Map.Entry<String, Object> expectedEntry : expectedMap.entrySet()) {

            String key = expectedEntry.getKey();
            String pathWithKey = path + "." + key;

            Object expectedValue = expectedEntry.getValue();
            Object actualValue = actualMap.get(key);

            if ((expectedValue instanceof List) && (actualValue instanceof List)) {

                List expectedValueCollection = (List) expectedValue;
                List actualValueCollection = (List) actualValue;

                if (!actualValueCollection.isEmpty()) {
                    //Get first Item to check the instance
                    Object actualValueCollectionItem = actualValueCollection.get(0);

                    if ((actualValueCollectionItem instanceof Map)) {

                        for (int i = 0; i < expectedValueCollection.size(); i++) {
                            String pathWithKeyAndIndex = pathWithKey + "." + i;


                            Object expectedValueItem = expectedValueCollection.get(i);
                            Object actualValueItem =
                                i < actualValueCollection.size()
                                    ? actualValueCollection.get(i)
                                    : null;

                            assertValue(expectedValueItem, actualValueItem, pathWithKeyAndIndex);

                        }
                    } else {
                        List<Object> expectedValuesStrings = expectedValueCollection;
                        List<Object> actualValuesStrings = actualValueCollection;
                        //The collection was a list of objects assert them using any order
                        assertThat(expectedValuesStrings, containsInAnyOrder(actualValuesStrings.toArray()));
                        assertEquals(expectedValuesStrings.size(), actualValuesStrings.size());
                    }
                } else {
                    //The collection was empty
                    assertThat(actualValue, equalTo(expectedValue));
                }

            } else {

                assertValue(expectedValue, actualValue, pathWithKey);
            }
        }
    }

    private void assertValue(
        Object expectedValue,
        Object actualValue,
        String path
    ) {
        if ((expectedValue instanceof Map) && (actualValue instanceof Map)) {

            assertFields(
                (Map<String, Object>) expectedValue,
                (Map<String, Object>) actualValue,
                path
            );

        } else {

            if ((expectedValue instanceof String) && (actualValue instanceof String)) {

                String expectedValueString = (String) expectedValue;
                String actualValueString = (String) actualValue;

                if (expectedValueString.equals("{$VERIFIER-UUID}")) {

                    assertTrue(
                        "Expected field did not match UUID regular expression (" + path + ")",
                        actualValueString.matches(UUID_REGEX_PATTERN)
                    );
                } else if (VERIFIER_ZONED_DATETIME_TODAY_WORKING_DAYS_PATTERN.matcher(expectedValueString).find()) {

                    expectedValueString = expectedValueString.replace("VERIFIER-", "");
                    String expandedExpectedDate = mapValueExpander.expandDateTimeToday(expectedValueString);

                    Date expectedDate = null;
                    try {
                        expectedDate = DATE_FORMATTER.parse(expandedExpectedDate);

                    } catch (ParseException e) {
                        fail("Could not parse expected date in (" + path + ")");
                    }

                    Date actualDate = null;

                    try {
                        actualDate = DATE_FORMATTER.parse(actualValueString);

                    } catch (ParseException e) {
                        fail("Could not parse actual date in (" + path + ")");
                    }

                    assertEquals(
                        "Expected field did not match actual (" + path + ")",
                        expectedDate,
                        actualDate
                    );
                } else if (expectedValueString.length() > 3
                           && expectedValueString.startsWith("$/")
                           && expectedValueString.endsWith("/")) {

                    expectedValueString = expectedValueString.substring(2, expectedValueString.length() - 1);

                    assertThat(
                        "Expected field matches regular expression (" + path + ")",
                        actualValueString,
                        matchesPattern(expectedValueString)
                    );
                } else {
                    assertThat(
                        "Expected field matches (" + path + ")",
                        actualValue,
                        equalTo(expectedValue)
                    );
                }
            } else {
                assertThat(
                    "Expected field matches (" + path + ")",
                    actualValue,
                    equalTo(expectedValue)
                );
            }
        }
    }
}
