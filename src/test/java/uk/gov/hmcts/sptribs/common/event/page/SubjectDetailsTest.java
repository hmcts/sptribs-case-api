package uk.gov.hmcts.sptribs.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.testutil.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubjectDetailsTest {

    @InjectMocks
    private SubjectDetails subjectDetails;

    @Test
    void shouldHaveErrorsPopulatedForAddressFields() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("Street1")
            .addressLine2("addrLine2")
            .build();
        final CicCase cicCase = CicCase.builder()
            .contactPreferenceType(ContactPreferenceType.POST)
            .address(addressGlobalUK)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = subjectDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(3);
    }

    @Test
    void shouldBeSuccessForValidAddressFields() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("Street1")
            .country("UK")
            .postCode("postcode")
            .county("Oxfordshire")
            .build();
        final CicCase cicCase = CicCase.builder()
            .contactPreferenceType(ContactPreferenceType.POST)
            .address(addressGlobalUK)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = subjectDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldBeSuccessForEmail() {
        //Given
        final AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("Street1")
            .country("UK")
            .postCode("postcode")
            .county("Oxfordshire")
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .address(addressGlobalUK)
            .contactPreferenceType(ContactPreferenceType.EMAIL)
            .email(TestConstants.TEST_SOLICITOR_EMAIL)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = subjectDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isEmpty();
    }
}
