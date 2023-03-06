package uk.gov.hmcts.sptribs.testutil;

import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import java.time.LocalDate;

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

    public static final String AUTH_HEADER_VALUE = "auth-header-value";
    public static final String INVALID_AUTH_TOKEN = "invalid_token";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
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
    public static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";

    public static final String ENGLISH_TEMPLATE_ID = "divorceminiapplication";
    public static final String WELSH_TEMPLATE_ID = "FL-DIV-GNO-WEL-00256.docx";
    public static final String BEARER = "Bearer ";
    public static final String FILENAME = "file-1616591401473378.pdf";

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
