package uk.gov.hmcts.sptribs.ciccase.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.CicaCaseTestHelper.createCicaCaseEntity;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class CicaCaseServiceTest {

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private IdamService idamService;

    @Mock
    private User user;

    @InjectMocks
    private CicaCaseService cicaCaseService;

    @Test
    void shouldReturnCaseWhenFoundByCCDReferenceAndEmail() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity expectedEntity = createCicaCaseEntity(ccdReference);

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(expectedEntity));

        // When
        CicaCaseEntity actualEntity = cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION);

        // Then
        assertThat(actualEntity).isEqualTo(expectedEntity);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);
        verify(caseDataRepository).checkCaseExists(ccdReference);
        verify(caseDataRepository).findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL);
    }

    private UserDetails userDetails() {
        return UserDetails
            .builder()
            .id(SYSTEM_USER_USER_ID)
            .email(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }

}




