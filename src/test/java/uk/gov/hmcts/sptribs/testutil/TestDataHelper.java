package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.CloseReason;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_2;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

public class TestDataHelper {

    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 4, 28, 1, 0);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private TestDataHelper() {

    }

    public static CaseData caseData() {
        return CaseData.builder()
            .build();
    }

    public static CaseData closedCaseData() {
        return CaseData.builder()
            .caseStatus(State.CaseManagement)
            .build();
    }

    public static CaseData awaitingOutcomeData() {

        CloseCase closeCase = new CloseCase();
        closeCase.setCloseCaseReason(CloseReason.Rejected);
        closeCase.setAdditionalDetail("case rejected");

        return CaseData.builder()
            .caseStatus(State.AwaitingOutcome)
            .closeCase(closeCase)
            .build();
    }

    public static CallbackRequest callbackRequest(CaseData caseData) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        return CallbackRequest
            .builder()
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData,
                                                  final String eventId) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(caseDetailsBefore(caseData))
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData, String eventId, String state) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                caseDetailsBefore(caseData))
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .state(state)
                    .id(TEST_CASE_ID)
                    .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
                    .build()
            )
            .build();
    }

    public static FeignException feignException(int status, String reason) {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        return FeignException.errorStatus(
            "idamRequestFailed",
            Response.builder()
                .request(request)
                .status(status)
                .headers(Collections.emptyMap())
                .reason(reason)
                .build()
        );
    }

    private static CaseDetails caseDetailsBefore(CaseData caseData) {
        return CaseDetails
            .builder()
            .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
            .id(TEST_CASE_ID)
            .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
            .build();
    }

    public static User getUser() {
        UserDetails userDetails = UserDetails
            .builder()
            .forename("testFname")
            .surname("testSname")
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }

    public static RecordListing getRecordListing() {
        final RecordListing recordListing = new RecordListing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setConferenceCallNumber("");
        recordListing.setHearingType(HearingType.FINAL);
        recordListing.setImportantInfoDetails("some details");
        recordListing.setVideoCallLink("");
        recordListing.setHearingDate(LocalDate.now());
        recordListing.setHearingTime("10:00");
        return recordListing;
    }

    public static RecordListing getRecordListingWithOneHearingDate() {
        final RecordListing recordListing = new RecordListing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setConferenceCallNumber("");
        recordListing.setHearingType(HearingType.FINAL);
        recordListing.setImportantInfoDetails("some details");
        recordListing.setVideoCallLink("");
        recordListing.setAdditionalHearingDate(getAdditionalHearingDatesOneDate());
        return recordListing;
    }


    public static List<ListValue<HearingDate>> getAdditionalHearingDatesOneDate() {
        HearingDate date1 = HearingDate.builder().hearingVenueDate(HEARING_DATE_1).hearingVenueTime(HEARING_TIME).build();
        List<ListValue<HearingDate>> list = new ArrayList<>();
        ListValue<HearingDate> listValue1 = ListValue.<HearingDate>builder().value(date1).id("0").build();
        list.add(listValue1);
        return list;
    }

    public static List<ListValue<HearingDate>> getAdditionalHearingDates() {
        HearingDate date1 = HearingDate.builder().hearingVenueDate(HEARING_DATE_1).hearingVenueTime(HEARING_TIME).build();
        HearingDate date2 = HearingDate.builder().hearingVenueDate(HEARING_DATE_2).hearingVenueTime(HEARING_TIME).build();
        List<ListValue<HearingDate>> list = new ArrayList<>();
        ListValue<HearingDate> listValue1 = ListValue.<HearingDate>builder().value(date1).id("0").build();
        ListValue<HearingDate> listValue2 = ListValue.<HearingDate>builder().value(date2).id("1").build();
        list.add(listValue1);
        list.add(listValue2);
        return list;
    }
}
