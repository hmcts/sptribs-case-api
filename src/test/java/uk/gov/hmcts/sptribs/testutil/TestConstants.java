package uk.gov.hmcts.sptribs.testutil;

import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TestConstants {
    public static final String TEST_SUBJECT_EMAIL = "subject2@test.com";
    public static final String TEST_USER_EMAIL = "test@test.com";
    public static final String TEST_APPLICANT_EMAIL = "applicant@test.com";
    public static final String TEST_SOLICITOR_EMAIL = "solicitor@test.com";
    public static final String TEST_SOLICITOR_NAME = "The Solicitor";
    public static final String TEST_FIRST_NAME = "test_first_name";
    public static final String TEST_MIDDLE_NAME = "test_middle_name";
    public static final String TEST_LAST_NAME = "test_last_name";

    public static final String APPLICANT_FIRST_NAME = "applicant_2_first_name";

    public static final String ABOUT_TO_START_URL = "/callbacks/about-to-start";
    public static final String ABOUT_TO_SUBMIT_URL = "/callbacks/about-to-submit";
    public static final String SUBMITTED_URL = "/callbacks/submitted";
    public static final String RECORD_NOTIFY_PARTIES_MID_EVENT_URL = "/callbacks/mid-event?page=recordListingNotifyPage";
    public static final String HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL = "/callbacks/mid-event?page=hearingOptionsRegionData";
    public static final String EDIT_DRAFT_ORDER_MID_EVENT_URL = "callbacks/mid-event?page=editDraftOrderAddDocumentFooter";
    public static final String EDIT_RECORD_LISTING_MID_EVENT_URL = "callbacks/mid-event?page=regionInfo";
    public static final String CHANGE_SECURITY_CLASSIFICATION_MID_EVENT_URL = "/callbacks/mid-event?page=changeSecurityClass";
    public static final String CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL
        = "/callbacks/mid-event?page=contactPartiesSelectDocument";
    public static final String EDIT_HEARING_SUMMARY_SELECT_MID_EVENT_URL
        = "/callbacks/mid-event?page=editHearingSummarySelect";
    public static final String DOCUMENT_MANAGEMENT_SELECT_DOCUMENT_MID_EVENT_URL
        = "/callbacks/mid-event?page=selectCaseDocuments";

    public static final String REFER_TO_JUDGE_REASON_MID_EVENT_URL = "/callbacks/mid-event?page=referToJudgeReason";

    public static final String ISSUE_DECISION_MID_EVENT_URL = "/callbacks/mid-event?page=issueDecisionAddDocumentFooter";

    public static final String AUTH_HEADER_VALUE = "auth-header-value";
    public static final String INVALID_AUTH_TOKEN = "invalid_token";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCEPT_VALUE = "application/vnd.jrd.api+json;Version=2.0";
    public static final String TEST_AUTHORIZATION_TOKEN = "test-auth";
    public static final String TEST_SYSTEM_AUTHORISATION_TOKEN = "test-system-auth";

    public static final String CCD_DATA = "ccd_data";

    public static final String SYSTEM_UPDATE_AUTH_TOKEN = "Bearer SystemUpdateAuthToken";
    public static final String CASEWORKER_USER_ID = "2";
    public static final String SYSTEM_USER_USER_ID = "4";

    public static final String TEST_CASEWORKER_USER_EMAIL = "testcw@test.com";
    public static final String TEST_SYSTEM_UPDATE_USER_EMAIL = "testsystem@test.com";
    public static final String TEST_SYSTEM_USER_PASSWORD = "testpassword";

    public static final Long TEST_CASE_ID = 1616591401473378L;
    public static final String TEST_CASE_ID_HYPHENATED = "1616-5914-0147-3378";
    public static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";

    public static final String ENGLISH_TEMPLATE_ID = "sptribsminiapplication";
    public static final String WELSH_TEMPLATE_ID = "FL-SPTRIBS-GNO-WEL-00256.docx";
    public static final String BEARER = "Bearer ";
    public static final String FILENAME = "file-1616591401473378.pdf";
    public static final LocalDateTime LOCAL_DATE_TIME =
        LocalDateTime.of(2022, 2, 22, 16, 21);
    public static final String CASE_DATA_CIC_ID = "CIC";
    public static final String CASE_DATA_FILE_CIC = "CICCaseData.json";
    public static final String CASE_TEST_AUTHORIZATION = "testAuth";
    public static final String CASE_CREATE_FAILURE_MSG = "Failing while creating the case ";
    public static final String CASE_UPDATE_FAILURE_MSG = "Failing while updating the case ";
    public static final String CASE_FETCH_FAILURE_MSG = "Failing while fetching the case details ";
    public static final String DOCUMENT_DELETE_FAILURE_MSG =
        "Failing while deleting the document. The error message is ";
    public static final String DOCUMENT_UPLOAD_FAILURE_MSG =
        "Failing while uploading the document. The error message is ";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String JSON_FILE_TYPE = "json";
    public static final String RESPONSE_STATUS_SUCCESS = "Success";
    public static final String TEST_CASE_EMAIL_ADDRESS = "test1@test.com";
    public static final String TEST_RESOURCE_NOT_FOUND = "Could not find resource in path";
    public static final String TEST_UPDATE_CASE_EMAIL_ADDRESS = "testUpdate@test.com";
    public static final String TEST_URL = "TestUrl";
    public static final String TEST_USER = "TestUser";
    public static final UUID TEST_CASE_DATA_FILE_UUID = UUID.randomUUID();

    public static final AddressGlobalUK APPLICANT_ADDRESS = AddressGlobalUK.builder()
        .addressLine1("line1")
        .addressLine2("line2")
        .postTown("city")
        .postCode("postcode")
        .build();

    public static final AddressGlobalUK SUBJECT_ADDRESS = AddressGlobalUK.builder()
        .addressLine1("line1")
        .addressLine2("line2")
        .postTown("city")
        .postCode("postcode")
        .build();

    public static final AddressGlobalUK SOLICITOR_ADDRESS = AddressGlobalUK.builder()
        .addressLine1("line1")
        .addressLine2("line2")
        .postTown("city")
        .postCode("postcode")
        .build();

    public static final LocalDate HEARING_DATE_1 = LocalDate.now();
    public static final LocalDate HEARING_DATE_2 = LocalDate.now().minusDays(1);
    public static final String HEARING_TIME = "11:00";

    private TestConstants() {
    }
}
