package uk.gov.hmcts.sptribs.ciccase.validation;

import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.MarriageDetails;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public final class ValidationUtil {

    public static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    public static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";
    public static final String IN_THE_FUTURE = " can not be in the future.";
    public static final String EMPTY = " cannot be empty or null";
    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";
    public static final String SOT_REQUIRED = "Statement of truth must be accepted by the person making the application";

    private ValidationUtil() {
    }



    public static List<String> validateApplicant2RequestChanges(Application application) {
        return flattenLists(
            notNull(application.getApplicant2ConfirmApplicant1Information(), "Applicant2ConfirmApplicant1Information"),
            notNull(application.getApplicant2ExplainsApplicant1IncorrectInformation(), "Applicant2ExplainsApplicant1IncorrectInformation")
        );
    }

    public static List<String> notNull(Object value, String field) {
        return value == null ? List.of(field + EMPTY) : emptyList();
    }

    public static List<String> validateMarriageDate(LocalDate localDate, String field) {
        if (localDate == null) {
            return List.of(field + EMPTY);
        } else if (isLessThanOneYearAgo(localDate)) {
            return List.of(field + LESS_THAN_ONE_YEAR_AGO);
        } else if (isOverOneHundredYearsAgo(localDate)) {
            return List.of(field + MORE_THAN_ONE_HUNDRED_YEARS_AGO);
        } else if (isInTheFuture(localDate)) {
            return List.of(field + IN_THE_FUTURE);
        }
        return emptyList();
    }

    public static List<String> validateJurisdictionConnections(CaseData caseData) {
        if (caseData.getApplication().isPaperCase()) {
            if (isEmpty(caseData.getApplication().getJurisdiction().getConnections())) {
                return List.of("JurisdictionConnections" + EMPTY);
            }
            return emptyList();
        }

        return caseData.getApplication().getJurisdiction().validateJurisdiction(caseData);
    }

    private static boolean isLessThanOneYearAgo(LocalDate date) {
        return !date.isAfter(LocalDate.now())
            && date.isAfter(LocalDate.now().minus(1, YEARS));
    }

    private static boolean isOverOneHundredYearsAgo(LocalDate date) {
        return date.isBefore(LocalDate.now().minus(100, YEARS));
    }

    private static boolean isInTheFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public static List<String> validateCaseFieldsForIssueApplication(MarriageDetails marriageDetails) {
        //MarriageApplicant1Name and MarriageDate are validated in validateBasicCase
        return flattenLists(
            notNull(marriageDetails.getApplicant2Name(), "MarriageApplicant2Name"),
            notNull(marriageDetails.getPlaceOfMarriage(), "PlaceOfMarriage")
        );
    }

    @SafeVarargs
    public static <E> List<E> flattenLists(List<E>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(toList());
    }
}
