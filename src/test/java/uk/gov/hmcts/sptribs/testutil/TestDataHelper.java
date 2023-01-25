package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.KeyValue;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.CloseReason;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseInvite;
import uk.gov.hmcts.sptribs.ciccase.model.Gender;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.HelpWithFees;
import uk.gov.hmcts.sptribs.ciccase.model.Jurisdiction;
import uk.gov.hmcts.sptribs.ciccase.model.MarriageDetails;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.sptribs.payment.model.FeeResponse;
import uk.gov.hmcts.sptribs.payment.model.Payment;
import uk.gov.hmcts.sptribs.payment.model.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.FEMALE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.MALE;
import static uk.gov.hmcts.sptribs.ciccase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_2;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER_EMAIL;

public class TestDataHelper {

    public static final LocalDate LOCAL_DATE = LocalDate.of(2021, 4, 28);
    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 4, 28, 1, 0);
    private static final String DOC_CONTROL_NUMBER = "61347040100200003";
    private static final LocalDateTime DOC_SCANNED_DATE_META_INFO = LocalDateTime.of(2022, 1, 1, 12, 12, 0);
    private static final String DOCUMENT_URL = "http://localhost:8080/documents/640055da-9330-11ec-b909-0242ac120002";
    private static final String DOCUMENT_BINARY_URL = "http://localhost:8080/documents/640055da-9330-11ec-b909-0242ac120002/binary";
    private static final String FILE_NAME = "61347040100200003.pdf";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private TestDataHelper() {

    }

    public static Applicant getApplicant() {
        return getApplicant(FEMALE);
    }

    public static Applicant getApplicant(Gender gender) {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .gender(gender)
            .languagePreferenceWelsh(NO)
            .contactDetailsType(PUBLIC)
            .financialOrder(NO)
            .build();
    }

    public static Applicant getApplicantWithAddress() {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .address(AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .country("UK")
                .build())
            .build();
    }

    public static Applicant getApplicant2(Gender gender) {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(gender)
            .build();
    }

    public static Applicant getJointApplicant2(Gender gender) {
        return Applicant.builder()
            .gender(gender)
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
    }



    public static Applicant respondent() {
        return Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(MALE)
            .build();
    }


    public static CaseData caseData() {
        return CaseData.builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .caseInvite(new CaseInvite(null, null, null))
            .build();
    }

    public static CaseData closedCaseData() {
        return CaseData.builder()
            .caseStatus(State.CaseManagement)
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .caseInvite(new CaseInvite(null, null, null))
            .build();
    }

    public static CaseData awaitingOutcomeData() {

        CloseCase closeCase = new CloseCase();
        closeCase.setCloseCaseReason(CloseReason.Rejected);
        closeCase.setAdditionalDetail("case rejected");

        return CaseData.builder()
            .caseStatus(State.AwaitingOutcome)
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .closeCase(closeCase)
            .caseInvite(new CaseInvite(null, null, null))
            .build();
    }

    public static CaseData caseDataWithOrderSummary() {
        var caseData = validApplicant1CaseData();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummaryWithFee());

        return caseData;
    }



    public static CaseData validJointApplicant1CaseData() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setMarriedInUk(YES);

        var applicant1 = getApplicant();
        applicant1.setContactDetailsType(PRIVATE);
        applicant1.setFinancialOrder(NO);
        applicant1.setLegalProceedings(NO);
        applicant1.setFirstName(TEST_FIRST_NAME);
        applicant1.setLastName(TEST_LAST_NAME);

        var application = Application.builder()
            .marriageDetails(marriageDetails)
            .jurisdiction(getJurisdiction())
            .applicant1HelpWithFees(
                HelpWithFees.builder()
                    .needHelp(NO)
                    .build()
            )
            .build();

        return CaseData
            .builder()
            .applicant1(applicant1)
            .applicant2(getJointApplicant2(MALE))
            .caseInvite(CaseInvite.builder().applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL).build())
            .divorceOrDissolution(DIVORCE)
            .application(application)
            .applicationType(JOINT_APPLICATION)
            .build();
    }

    public static CaseData validApplicant1CaseData() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant2(getApplicant2(MALE));
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        return caseData;
    }


    public static Payment payment(final int amount, final PaymentStatus paymentStatus) {
        return Payment.builder()
            .created(LocalDateTime.now())
            .updated(LocalDateTime.now())
            .amount(amount)
            .channel("online")
            .feeCode("FEE0001")
            .status(paymentStatus)
            .reference("paymentRef")
            .transactionId("ge7po9h5bhbtbd466424src9tk")
            .build();
    }



    public static Jurisdiction getJurisdiction() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(APP_1_APP_2_RESIDENT));
        jurisdiction.setApplicant1Residence(YES);
        jurisdiction.setApplicant2Residence(YES);

        return jurisdiction;
    }








    public static CallbackRequest callbackRequest() {
        return callbackRequest(caseDataWithOrderSummary());
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

    public static FeeResponse getFeeResponse() {
        return FeeResponse
            .builder()
            .feeCode(FEE_CODE)
            .amount(10.0)
            .description(ISSUE_FEE)
            .version(1)
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









    public static OrganisationPolicy<UserRole> organisationPolicy() {
        return OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationName(TEST_ORG_NAME)
                .organisationId(TEST_ORG_ID)
                .build())
            .build();
    }
















    public static DynamicList getPbaNumbersForAccount(String accountNumber) {
        return DynamicList
            .builder()
            .value(
                DynamicListElement
                    .builder()
                    .code(UUID.randomUUID())
                    .label(accountNumber)
                    .build()
            )
            .build();
    }

    private static CaseDetails caseDetailsBefore(CaseData caseData) {
        return CaseDetails
            .builder()
            .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
            .id(TEST_CASE_ID)
            .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
            .build();
    }

    public static ListValue<Fee> getFeeListValue() {
        return ListValue
            .<Fee>builder()
            .value(Fee
                .builder()
                .amount("550")
                .description("fees for divorce")
                .code("FEE002")
                .build()
            )
            .build();
    }

    public static OrderSummary orderSummaryWithFee() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .fees(singletonList(getFeeListValue()))
            .build();
    }



    public static OcrDataValidationRequest ocrDataValidationRequest() {
        return OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    KeyValue.builder()
                        .key("applicant1Name")
                        .value("bob")
                        .build())
            )
            .build();
    }





    public static KeyValue populateKeyValue(String key, String value) {
        return KeyValue.builder()
            .key(key)
            .value(value)
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
