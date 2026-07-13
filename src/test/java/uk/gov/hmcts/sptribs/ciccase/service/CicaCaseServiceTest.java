package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CicaCaseService cicaCaseService;

    @Test
    void whenGetCaseByCCDReference_thenShouldReturnCaseSuccessfully() {
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

    @Test
    void whenGetCaseByCCDReference_thenThrowUnauthorisedCaseAccessExceptionAsEmailNotPresent() {
        // Given
        String ccdReference = "1234567891234567";

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.empty());

        // When & Then
        UnauthorisedCaseAccessException exception = assertThrows(
            UnauthorisedCaseAccessException.class,
            () -> cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)
        );

        // Then
        assertEquals("User is not authorised to access case: " + ccdReference, exception.getMessage());

        verify(caseDataRepository).findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);

    }

    @Test
    void whenGetCaseByCCDReference_thenThrowCaseNotFoundException() {
        // Given
        String ccdReference = "1234567891234567";
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(false);

        // When & Then
        CaseNotFoundException exception = assertThrows(
            CaseNotFoundException.class,
            () -> cicaCaseService.getCaseByCCDReference(ccdReference, TEST_AUTHORIZATION)
        );

        // Then
        assertEquals("No case found with CCD reference: " + ccdReference, exception.getMessage());

        verify(caseDataRepository).checkCaseExists(ccdReference);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);
        verifyNoMoreInteractions(caseDataRepository);
    }

    @Test
    void whenPostcodesMatchExactly_thenShouldValidateSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, postcode);

        // When & Then - should not throw any exception
        cicaCaseService.validatePostcode(entity, postcode);
    }

    @Test
    void whenGenericEuropeanPostcodesMatchExactly_thenShouldValidateSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "11180";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, postcode);

        // When & Then - should not throw any exception
        cicaCaseService.validatePostcode(entity, postcode);
    }

    @Test
    void whenGenericEuropeanPostcodeWithDashMatchExactly_thenShouldValidateSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "111-80";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, postcode);

        // When & Then - should not throw any exception
        cicaCaseService.validatePostcode(entity, postcode);
    }


    @Test
    void whenPostcodesMatchWithDifferentCasesAndWhitespace_thenShouldValidateSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, "sw11 1pd");

        // When & Then - should ignore case and spaces
        cicaCaseService.validatePostcode(entity, "SW111PD  ");
    }

    @Test
    void whenRepresentativePostcodesMatchExactly_thenShouldValidateSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";
        CicaCaseEntity entity = createCicaCaseEntityWithRepresentative(ccdReference, postcode);

        // When & Then - should not throw any exception
        cicaCaseService.validatePostcode(entity, postcode);
    }


    @Test
    void whenAppellantPostcodesMatchExactly_thenShouldValidateSuccessfully() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";
        CicaCaseEntity entity = createCicaCaseEntityWithAppellant(ccdReference, postcode);

        // When & Then - should not throw any exception
        cicaCaseService.validatePostcode(entity, postcode);
    }

    @Test
    void whenPostcodesDoNotMatch_thenShouldThrowUnauthorisedPostcodeException() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, "SW11 1PD");

        // When & Then
        UnauthorisedCaseAccessException exception = assertThrows(
            UnauthorisedCaseAccessException.class,
            () -> cicaCaseService.validatePostcode(entity, "EH12 3AB")
        );
        assertEquals("Submitted postcode does not match the postcode held in case data", exception.getMessage());
    }

    @Test
    void whenSubmittedPostcodeIsNull_thenShouldThrowUnauthorisedPostcodeException() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, "SW11 1PD");

        // When & Then
        UnauthorisedCaseAccessException exception = assertThrows(
            UnauthorisedCaseAccessException.class,
            () -> cicaCaseService.validatePostcode(entity, null)
        );
        assertEquals("Submitted postcode cannot be null", exception.getMessage());
    }

    @Test
    void whenStoredPostcodeIsNull_thenShouldThrowUnauthorisedPostcodeException() {
        // Given
        String ccdReference = "1234567891234567";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, null);

        // When & Then
        UnauthorisedCaseAccessException exception = assertThrows(
            UnauthorisedCaseAccessException.class,
            () -> cicaCaseService.validatePostcode(entity, "SW11 1PD")
        );
        assertEquals("Postcode not found in case data", exception.getMessage());
    }

    @Test
    void whenGetCaseWithVerifiedPostcode_thenValidateAndReturn() {
        // Given
        String ccdReference = "1234567891234567";
        String postcode = "SW11 1PD";
        CicaCaseEntity entity = createCicaCaseEntityWithPostcode(ccdReference, postcode);

        when(user.getUserDetails()).thenReturn(userDetails());
        when(idamService.retrieveUser(TEST_AUTHORIZATION)).thenReturn(user);
        when(caseDataRepository.checkCaseExists(ccdReference)).thenReturn(true);
        when(caseDataRepository.findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL))
            .thenReturn(Optional.of(entity));

        // When
        CicaCaseEntity result = cicaCaseService.getCaseWithVerifiedPostcode(ccdReference, TEST_AUTHORIZATION, postcode);

        // Then
        assertThat(result).isEqualTo(entity);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION);
        verify(caseDataRepository).checkCaseExists(ccdReference);
        verify(caseDataRepository).findCase(ccdReference, TEST_SYSTEM_UPDATE_USER_EMAIL);
    }

    private CicaCaseEntity createCicaCaseEntityWithPostcode(String ccdReference, String postcode) {
        CaseData caseData = new CaseData();
        CicCase cicCase = new CicCase();
        AddressGlobalUK address = AddressGlobalUK.builder().postCode(postcode).build();
        cicCase.setAddress(address);
        cicCase.setEmail(TEST_SYSTEM_UPDATE_USER_EMAIL);
        caseData.setCicCase(cicCase);

        return CicaCaseEntity.builder()
            .id(ccdReference)
            .state("Submitted")
            .data(objectMapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build();
    }

    private CicaCaseEntity createCicaCaseEntityWithRepresentative(String ccdReference, String postcode) {
        CaseData caseData = new CaseData();
        CicCase cicCase = new CicCase();
        AddressGlobalUK address = AddressGlobalUK.builder().postCode(postcode).build();
        cicCase.setAddress(address);
        cicCase.setRepresentativeEmailAddress(TEST_SYSTEM_UPDATE_USER_EMAIL);
        caseData.setCicCase(cicCase);

        return CicaCaseEntity.builder()
            .id(ccdReference)
            .state("Submitted")
            .data(objectMapper.convertValue(caseData, new TypeReference<Map<String, JsonNode>>() {}))
            .build();
    }

    private CicaCaseEntity createCicaCaseEntityWithAppellant(String ccdReference, String postcode) {
        CaseData caseData = new CaseData();
        CicCase cicCase = new CicCase();
        AddressGlobalUK address = AddressGlobalUK.builder().postCode(postcode).build();
        cicCase.setAddress(address);
        cicCase.setApplicantEmailAddress(TEST_SYSTEM_UPDATE_USER_EMAIL);
        caseData.setCicCase(cicCase);

        return CicaCaseEntity.builder()
            .id(ccdReference)
            .state("Submitted")
            .data(objectMapper.convertValue(caseData, new TypeReference<Map<String, JsonNode>>() {}))
            .build();
    }

    private UserDetails userDetails() {
        return UserDetails
            .builder()
            .id(SYSTEM_USER_USER_ID)
            .email(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }

}




