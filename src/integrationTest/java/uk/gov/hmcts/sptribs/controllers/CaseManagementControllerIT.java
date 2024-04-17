package uk.gov.hmcts.sptribs.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.exception.CaseCreateOrUpdateException;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_CREATE_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_SUBMIT_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_UPDATE_CASE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CaseManagementControllerIT {

    private static final String CCD_CASE_RESPONSE =
        "classpath:citizen-create-case-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSuccessfullyCreateCaseWhenCreateCaseControllerTriggered() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("CIC")
            .build();

        final CaseData caseData = CaseData.builder()
            .dssCaseData(dssCaseData)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails reformCaseDetails =
            objectMapper.convertValue(caseDetails, new TypeReference<>() {});

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_CREATE_CASE)
            .token("startEventToken")
            .caseDetails(reformCaseDetails)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_CREATE_CASE)
                .build()
            )
            .eventToken("startEventToken")
            .build();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(coreCaseDataApi.startForCitizen(
            "test-auth",
            "s2s token",
            "2",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            CITIZEN_CIC_CREATE_CASE
        )).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitForCitizen(
            "test-auth",
            "s2s token",
            "2",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        String response = mockMvc.perform(post("/case/dss-orchestration/create")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(dssCaseData))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CCD_CASE_RESPONSE)));
    }

    @Test
    void shouldThrowErrorIfCreateCaseControllerCalledWithInvalidCaseType() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("ST_CIC")
            .build();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        mockMvc.perform(post("/case/dss-orchestration/create")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(dssCaseData))
            .accept(APPLICATION_JSON))
            .andExpect(status().is5xxServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof CaseCreateOrUpdateException));
    }

    @Test
    void shouldSuccessfullyUpdateCaseWhenUpdateCaseControllerTriggered() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("CIC")
            .build();

        final CaseData caseData = CaseData.builder()
            .dssCaseData(dssCaseData)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails reformCaseDetails =
            objectMapper.convertValue(caseDetails, new TypeReference<>() {});

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_CREATE_CASE)
            .token("startEventToken")
            .caseDetails(reformCaseDetails)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_UPDATE_CASE)
                .build()
            )
            .eventToken("startEventToken")
            .build();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(coreCaseDataApi.startEventForCitizen(
            "test-auth",
            "s2s token",
            "2",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            CITIZEN_CIC_UPDATE_CASE
        )).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(
            "test-auth",
            "s2s token",
            "2",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        String response = mockMvc.perform(put("/case/dss-orchestration/1616591401473378/update?event=UPDATE")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(dssCaseData))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CCD_CASE_RESPONSE)));
    }

    @Test
    void shouldThrowErrorIfUpdateCaseControllerCalledWithInvalidCaseType() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("ST_CIC")
            .build();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        mockMvc.perform(put("/case/dss-orchestration/1616591401473378/update?event=UPDATE")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(dssCaseData))
            .accept(APPLICATION_JSON))
            .andExpect(status().is5xxServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof CaseCreateOrUpdateException));
    }

    @Test
    void shouldSuccessfullySubmitCaseWhenSubmitCaseControllerTriggered() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("CIC")
            .build();

        final CaseData caseData = CaseData.builder()
            .dssCaseData(dssCaseData)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails reformCaseDetails =
            objectMapper.convertValue(caseDetails, new TypeReference<>() {});

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_SUBMIT_CASE)
            .token("startEventToken")
            .caseDetails(reformCaseDetails)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_SUBMIT_CASE)
                .build()
            )
            .eventToken("startEventToken")
            .build();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(coreCaseDataApi.startEventForCitizen(
            "test-auth",
            "s2s token",
            "2",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            CITIZEN_CIC_SUBMIT_CASE
        )).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(
            "test-auth",
            "s2s token",
            "2",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        String response = mockMvc.perform(put("/case/dss-orchestration/1616591401473378/update?event=SUBMIT")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(dssCaseData))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CCD_CASE_RESPONSE)));
    }

    @Test
    void shouldThrowErrorIfSubmitCaseControllerCalledWithInvalidCaseType() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("ST_CIC")
            .build();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        mockMvc.perform(put("/case/dss-orchestration/1616591401473378/update?event=SUBMIT")
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(dssCaseData))
            .accept(APPLICATION_JSON))
            .andExpect(status().is5xxServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof CaseCreateOrUpdateException));
    }

    @Test
    void shouldFetchCaseDetailsWhenControllerTriggered() throws Exception {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("CIC")
            .build();

        final CaseData caseData = CaseData.builder()
            .dssCaseData(dssCaseData)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails reformCaseDetails =
            objectMapper.convertValue(caseDetails, new TypeReference<>() {});

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(coreCaseDataApi.getCase(
            "test-auth",
            "s2s token",
            "1616591401473378"
        )).thenReturn(reformCaseDetails);

        String response = mockMvc.perform(get("/case/dss-orchestration/fetchCaseDetails/" + TEST_CASE_ID)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CCD_CASE_RESPONSE)));
    }
}
