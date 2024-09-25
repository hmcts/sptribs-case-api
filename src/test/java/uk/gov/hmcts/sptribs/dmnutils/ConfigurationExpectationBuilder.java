package uk.gov.hmcts.sptribs.dmnutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.CASE_NAME;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DESCRIPTION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DUE_DATE_INTERVAL_DAYS;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DUE_DATE_NON_WORKING_CALENDAR;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DUE_DATE_ORIGIN;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.DUE_DATE_WORKING_DAYS_OF_WEEK;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.LOCATION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.LOCATION_NAME;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.MAJOR_PRIORITY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.MINOR_PRIORITY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.PRIORITY_DATE_ORIGIN_REF;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.REGION;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.ROLE_CATEGORY;
import static uk.gov.hmcts.sptribs.dmnutils.CamundaTaskConstants.WORK_TYPE;

public class ConfigurationExpectationBuilder {

    public record ExpectedValue(String name, Object value, boolean canReconfigure) {}

    private static final List<String> EXPECTED_PROPERTIES = Arrays.asList(
        CASE_NAME, CASE_MANAGEMENT_CATEGORY, REGION, LOCATION, LOCATION_NAME, MAJOR_PRIORITY, MINOR_PRIORITY,
        DUE_DATE_NON_WORKING_CALENDAR, DUE_DATE_WORKING_DAYS_OF_WEEK, WORK_TYPE, ROLE_CATEGORY, DUE_DATE_INTERVAL_DAYS,
        DESCRIPTION, PRIORITY_DATE_ORIGIN_REF, DUE_DATE_ORIGIN
    );

    private final List<ExpectedValue> expectations = new ArrayList<>();

    public static ConfigurationExpectationBuilder defaultExpectations() {
        return new ConfigurationExpectationBuilder();
    }

    public List<ExpectedValue> build() {
        return expectations.stream()
            .filter(e -> EXPECTED_PROPERTIES.contains(e.name()))
            .toList();
    }

    public ConfigurationExpectationBuilder expectedValue(String name, Object value, boolean canReconfigure) {
        expectations.add(new ExpectedValue(name, value, canReconfigure));
        return this;
    }
}
