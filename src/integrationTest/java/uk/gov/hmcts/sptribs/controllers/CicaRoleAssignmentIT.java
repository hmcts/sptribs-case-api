package uk.gov.hmcts.sptribs.controllers;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseDataRepositoryImpl;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@SpringBootTest(properties = {"s2s-authorised.services=sptribs_frontend", "s2s.stub=false"})
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
class CicaRoleAssignmentIT {

    private static final String CASE_ID = "1234567891234567";
    private static final String URL = "/cases/cica/" + CASE_ID + "/role-assignments";
    private static final String CITIZEN_ID = "citizen-user-id";
    private static final String CITIZEN_EMAIL = "citizen@example.test";
    private static final String SYSTEM_TOKEN = "Bearer system-token";
    private static final String CCD_SERVICE_TOKEN = "ccd-service-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CaseDataRepositoryImpl caseDataRepository;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private AuthTokenValidator authTokenValidator;

    @BeforeAll
    static void startIdam() {
        IdamWireMock.start();
    }

    @AfterAll
    static void stopIdam() {
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void authoriseService() {
        when(authTokenValidator.getServiceName(TEST_SERVICE_AUTH_TOKEN)).thenReturn("sptribs_frontend");
    }

    @Test
    void shouldVerifyMembershipBeforeAssigningOnlyTheCallersCreatorRole() throws Exception {
        setUpCaseMember();
        setUpRoleLookup(roles());

        // Client-supplied identities and roles cannot override the authenticated caller or fixed role.
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"user_id\":\"another-user\",\"case_role\":\"[ADMIN]\"}"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        var calls = inOrder(caseDataRepository, caseAssignmentApi, idamService);
        calls.verify(idamService).retrieveUser(TEST_AUTHORIZATION_TOKEN);
        calls.verify(caseDataRepository).checkCaseExists(CASE_ID);
        calls.verify(caseDataRepository).checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL);
        calls.verify(idamService).retrieveUser(TEST_AUTHORIZATION_TOKEN);
        calls.verify(idamService).retrieveSystemUpdateUserDetails();
        calls.verify(caseAssignmentApi).getUserRoles(SYSTEM_TOKEN, CCD_SERVICE_TOKEN, CASE_ID, CITIZEN_ID);
        ArgumentCaptor<CaseAssignmentUserRolesRequest> submitted =
            ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);
        calls.verify(caseAssignmentApi).addCaseUserRoles(eq(SYSTEM_TOKEN), eq(CCD_SERVICE_TOKEN), submitted.capture());
        var assignedRoles = submitted.getValue().getCaseAssignmentUserRolesWithOrganisation();
        assertThat(assignedRoles).hasSize(1);
        assertThat(assignedRoles.getFirst().getUserId()).isEqualTo(CITIZEN_ID);
        assertThat(assignedRoles.getFirst().getCaseDataId()).isEqualTo(CASE_ID);
        assertThat(assignedRoles.getFirst().getCaseRole()).isEqualTo("[CREATOR]");
        assertThat(assignedRoles.getFirst().getOrganisationId()).isNull();
    }

    @Test
    void shouldStillCheckMembershipWhenCreatorRoleAlreadyExists() throws Exception {
        setUpCaseMember();
        setUpRoleLookup(roles(creatorRole()));

        assignRole().andExpect(status().isNoContent());

        verify(caseDataRepository).checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL);
        verify(caseAssignmentApi, never()).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldRejectNonMemberWithoutLookingUpOrAssigningRoles() throws Exception {
        setUpCaseMember();
        when(caseDataRepository.checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL)).thenReturn(false);

        assignRole().andExpect(status().isForbidden());

        verifyNoInteractions(caseAssignmentApi, authTokenGenerator);
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @Test
    void shouldRejectMissingCaseWithoutLookingUpOrAssigningRoles() throws Exception {
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(citizen(CITIZEN_ID));
        when(caseDataRepository.checkCaseExists(CASE_ID)).thenReturn(false);

        assignRole().andExpect(status().isNotFound());

        verifyNoInteractions(caseAssignmentApi, authTokenGenerator);
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @Test
    void shouldRejectFailedIdentityLookupWithoutAssigningRoles() throws Exception {
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenThrow(ccdFailure(401));

        assignRole().andExpect(status().isForbidden()).andExpect(content().string(""));

        verifyNoInteractions(caseDataRepository, caseAssignmentApi, authTokenGenerator);
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @Test
    void shouldRejectMissingCitizenIdEvenWhenEmailMatchesCase() throws Exception {
        setUpCaseMember();
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(citizen(" "));

        assignRole().andExpect(status().isForbidden());

        verifyNoInteractions(caseAssignmentApi, authTokenGenerator);
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "123456789123456a"})
    void shouldRejectInvalidReferenceBeforeCheckingMembership(String reference) throws Exception {
        mockMvc.perform(post("/cases/cica/" + reference + "/role-assignments")
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(idamService, caseDataRepository, caseAssignmentApi);
    }

    @Test
    void shouldRejectMissingUserToken() throws Exception {
        mockMvc.perform(post(URL).header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(idamService, caseDataRepository, caseAssignmentApi);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void shouldRejectBlankUserToken(String token) throws Exception {
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, token)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(idamService, caseDataRepository, caseAssignmentApi);
    }

    @Test
    void shouldRejectMissingServiceToken() throws Exception {
        mockMvc.perform(post(URL).header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN))
            .andExpect(status().isForbidden());

        verifyNoInteractions(idamService, caseDataRepository, caseAssignmentApi);
    }

    @Test
    void shouldRejectUnauthorisedServiceBeforeCheckingMembership() throws Exception {
        when(authTokenValidator.getServiceName(TEST_SERVICE_AUTH_TOKEN)).thenReturn("untrusted-service");

        assignRole().andExpect(status().isForbidden());

        verifyNoInteractions(idamService, caseDataRepository, caseAssignmentApi);
    }

    @Test
    void shouldNotAssignRolesWhenRoleLookupFailsOrExposeDownstreamBody() throws Exception {
        setUpCaseMember();
        setUpRoleLookup(roles());
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, CCD_SERVICE_TOKEN, CASE_ID, CITIZEN_ID))
            .thenThrow(ccdFailure(503));

        assignRole().andExpect(status().isServiceUnavailable()).andExpect(content().string(""));

        verify(caseAssignmentApi, never()).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldNotReportSuccessWhenAssignmentFailsAndRoleIsStillMissing() throws Exception {
        setUpCaseMember();
        setUpRoleLookup(roles());
        doThrow(ccdFailure(503)).when(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());

        assignRole().andExpect(status().isServiceUnavailable()).andExpect(content().string(""));

        verify(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldAcceptConcurrentAssignmentOnlyAfterConfirmingCreatorRole() throws Exception {
        setUpCaseMember();
        setUpRoleLookup(roles());
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, CCD_SERVICE_TOKEN, CASE_ID, CITIZEN_ID))
            .thenReturn(roles(), roles(creatorRole()));
        doThrow(ccdFailure(409)).when(caseAssignmentApi).addCaseUserRoles(anyString(), anyString(), any());

        assignRole().andExpect(status().isNoContent());
    }

    private void setUpCaseMember() {
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(citizen(CITIZEN_ID));
        when(caseDataRepository.checkCaseExists(CASE_ID)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL)).thenReturn(true);
    }

    private void setUpRoleLookup(CaseAssignmentUserRolesResource roles) {
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new CICUser(SYSTEM_TOKEN, UserInfo.builder().build()));
        when(authTokenGenerator.generate()).thenReturn(CCD_SERVICE_TOKEN);
        when(caseAssignmentApi.getUserRoles(SYSTEM_TOKEN, CCD_SERVICE_TOKEN, CASE_ID, CITIZEN_ID)).thenReturn(roles);
    }

    private CICUser citizen(String userId) {
        return new CICUser(TEST_AUTHORIZATION_TOKEN, UserInfo.builder().uid(userId).sub(CITIZEN_EMAIL).build());
    }

    private CaseAssignmentUserRolesResource roles(CaseAssignmentUserRole... roles) {
        return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(List.of(roles)).build();
    }

    private CaseAssignmentUserRole creatorRole() {
        return CaseAssignmentUserRole.builder().caseDataId(CASE_ID).userId(CITIZEN_ID).caseRole("[CREATOR]").build();
    }

    private ResultActions assignRole() throws Exception {
        return mockMvc.perform(post(URL)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN));
    }

    private FeignException ccdFailure(int status) {
        Request request = Request.create(Request.HttpMethod.POST, "http://ccd/case-users",
            Map.of(), null, StandardCharsets.UTF_8);
        Response response = Response.builder().status(status).request(request)
            .body("private downstream details", StandardCharsets.UTF_8).build();
        return FeignException.errorStatus("caseUserRoles", response);
    }
}
