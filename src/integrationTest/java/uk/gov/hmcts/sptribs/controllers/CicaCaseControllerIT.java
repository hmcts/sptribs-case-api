package uk.gov.hmcts.sptribs.controllers;

import feign.FeignException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseUpdateService;
import uk.gov.hmcts.sptribs.ciccase.service.CicaRoleService;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
class CicaCaseControllerIT {

    private static final String VALID_CCD_REFERENCE = "1234567891234567";
    private static final String ACCESS_CHECK_URL = "/cases/cica/" + VALID_CCD_REFERENCE + "/access";
    private static final String ROLE_ASSIGNMENTS_URL = "/cases/cica/" + VALID_CCD_REFERENCE + "/role-assignments";
    private static final String UPDATE_CASE_URL = "/cases/cica/" + VALID_CCD_REFERENCE + "/updates";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private CicaCaseService cicaCaseService;

    @MockitoBean
    private CicaRoleService cicaRoleService;

    @MockitoBean
    private CicaCaseUpdateService cicaCaseUpdateService;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldCheckAccessSuccessfully() throws Exception {
        // Given
        doNothing().when(cicaCaseService).checkIfUserHasAccess(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);

        // When & Then
        mockMvc.perform(get(ACCESS_CHECK_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenCcdReferenceIsInvalid() throws Exception {
        // Given
        String invalidCcdReference = "12345";
        String url = "/cases/cica/" + invalidCcdReference + "/access";

        // When & Then
        mockMvc.perform(get(url)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCcdReferenceContainsNonDigits() throws Exception {
        // Given
        String invalidCcdReference = "123456789123456a";
        String url = "/cases/cica/" + invalidCcdReference + "/access";

        // When & Then
        mockMvc.perform(get(url)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403WhenUserIsForbidden() throws Exception {
        // Given
        doThrow(new UnauthorisedCaseAccessException("User is not associated with this case"))
            .when(cicaCaseService).checkIfUserHasAccess(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);

        // When & Then
        mockMvc.perform(get(ACCESS_CHECK_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404WhenCaseNotFound() throws Exception {
        // Given
        doThrow(new CaseNotFoundException("Case not found"))
            .when(cicaCaseService).checkIfUserHasAccess(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);

        // When & Then
        mockMvc.perform(get(ACCESS_CHECK_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldAssignCreatorRoleToAuthenticatedCitizenWithCaseAccess() throws Exception {
        mockMvc.perform(post(ROLE_ASSIGNMENTS_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isNoContent());

        verify(cicaCaseService).checkIfUserHasAccess(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);
        verify(cicaRoleService).assignCreatorRole(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);
    }

    @Test
    void shouldNotAssignCreatorRoleWithoutCaseAccess() throws Exception {
        doThrow(new UnauthorisedCaseAccessException("User is not associated with this case"))
            .when(cicaCaseService).checkIfUserHasAccess(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(post(ROLE_ASSIGNMENTS_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isForbidden());

        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldNotAssignCreatorRoleWhenCaseDoesNotExist() throws Exception {
        doThrow(new CaseNotFoundException("Case not found"))
            .when(cicaCaseService).checkIfUserHasAccess(VALID_CCD_REFERENCE, TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(post(ROLE_ASSIGNMENTS_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isNotFound());

        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldNotAssignCreatorRoleWithoutUserToken() throws Exception {
        mockMvc.perform(post(ROLE_ASSIGNMENTS_URL)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cicaCaseService, cicaRoleService);
    }

    @Test
    void shouldNotAssignCreatorRoleWithInvalidCaseReference() throws Exception {
        mockMvc.perform(post("/cases/cica/invalid/role-assignments")
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cicaCaseService, cicaRoleService);
    }

    @Test
    void shouldUpdateCaseWithCitizensToken() throws Exception {
        CaseResource updatedCase = new CaseResource();
        updatedCase.setReference(VALID_CCD_REFERENCE);
        updatedCase.setState("CaseManagement");
        when(cicaCaseUpdateService.updateCase(org.mockito.ArgumentMatchers.eq(VALID_CCD_REFERENCE),
            org.mockito.ArgumentMatchers.eq(TEST_AUTHORIZATION_TOKEN), org.mockito.ArgumentMatchers.any()))
            .thenReturn(updatedCase);

        mockMvc.perform(post(UPDATE_CASE_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":{\"dssCaseDataAdditionalInformation\":\"Update\"}}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.case_id").value(VALID_CCD_REFERENCE))
            .andExpect(jsonPath("$.state").value("CaseManagement"));
    }

    @Test
    void shouldRejectUpdateWithoutUserToken() throws Exception {
        mockMvc.perform(post(UPDATE_CASE_URL)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":{}}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cicaCaseUpdateService);
    }

    @Test
    void shouldRejectUpdateWithInvalidReference() throws Exception {
        mockMvc.perform(post("/cases/cica/invalid/updates")
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":{}}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cicaCaseUpdateService);
    }

    @Test
    void shouldRejectUpdateWithoutCaseAccess() throws Exception {
        when(cicaCaseUpdateService.updateCase(org.mockito.ArgumentMatchers.eq(VALID_CCD_REFERENCE),
            org.mockito.ArgumentMatchers.eq(TEST_AUTHORIZATION_TOKEN), org.mockito.ArgumentMatchers.any()))
            .thenThrow(new UnauthorisedCaseAccessException("User is not associated with this case"));

        mockMvc.perform(post(UPDATE_CASE_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":{}}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnCcdFailureStatusWithoutExposingCcdResponse() throws Exception {
        FeignException ccdFailure = mock(FeignException.class);
        when(ccdFailure.status()).thenReturn(404);
        when(cicaCaseUpdateService.updateCase(org.mockito.ArgumentMatchers.eq(VALID_CCD_REFERENCE),
            org.mockito.ArgumentMatchers.eq(TEST_AUTHORIZATION_TOKEN), org.mockito.ArgumentMatchers.any()))
            .thenThrow(ccdFailure);

        mockMvc.perform(post(UPDATE_CASE_URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\":{}}"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }
}
