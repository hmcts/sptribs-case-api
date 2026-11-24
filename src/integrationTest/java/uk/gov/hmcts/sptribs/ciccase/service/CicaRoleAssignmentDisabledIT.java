package uk.gov.hmcts.sptribs.ciccase.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.document.DocumentDownloadService;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_DATA_FILE_UUID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@SpringBootTest(classes = {CicaCaseService.class, CicaRoleService.class, DocumentDownloadService.class},
    properties = "ROLE_ASSIGNMENT_ENABLED=false")
class CicaRoleAssignmentDisabledIT {

    @Autowired
    private CicaCaseService caseService;
    @Autowired
    private DocumentDownloadService downloadService;
    @MockitoBean
    private CaseDataRepository caseRepository;
    @MockitoBean
    private CaseAssignmentApi caseAssignmentApi;
    @MockitoBean
    private AuthTokenGenerator s2s;
    @MockitoBean
    private IdamService idam;
    @MockitoBean
    private CaseDocumentClientApi cdam;

    @Test
    void verifiesMembershipButDoesNotAccessCaseRoles() {
        String email = "citizen@example.test";
        when(idam.retrieveUser(TEST_AUTHORIZATION_TOKEN))
            .thenReturn(new CICUser(TEST_AUTHORIZATION_TOKEN, UserInfo.builder().sub(email).build()));
        when(caseRepository.checkCaseExists(TEST_CASE_ID_STRING)).thenReturn(true);
        when(caseRepository.checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, email)).thenReturn(true);

        caseService.ensureCreatorRole(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN);

        verify(caseRepository).checkIfUserHasAccessToCase(TEST_CASE_ID_STRING, email);
        verify(idam, never()).retrieveSystemUpdateUserDetails();
        verifyNoInteractions(caseAssignmentApi, s2s);
    }

    @Test
    void usesSystemUserAndS2sForBothMetadataAndBinary() throws Exception {
        String systemToken = "Bearer system-token";
        byte[] bytes = "test document".getBytes(StandardCharsets.UTF_8);
        when(idam.retrieveSystemUpdateUserDetails()).thenReturn(new CICUser(systemToken, null));
        when(s2s.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(cdam.getDocument(systemToken, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_DATA_FILE_UUID))
            .thenReturn(ResponseEntity.ok(new Document()));
        when(cdam.getDocumentBinary(systemToken, TEST_SERVICE_AUTH_TOKEN, TEST_CASE_DATA_FILE_UUID))
            .thenReturn(ResponseEntity.ok(bytes));

        var download = downloadService.downloadDocument(TEST_CASE_ID_STRING, TEST_AUTHORIZATION_TOKEN,
            TEST_CASE_DATA_FILE_UUID.toString());

        assertThat(download.file().getContentAsByteArray()).isEqualTo(bytes);
        verifyNoInteractions(caseAssignmentApi);
    }
}
