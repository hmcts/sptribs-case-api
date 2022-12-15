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
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RepresentativeDetailsTest {

    @InjectMocks
    private RepresentativeDetails representativeDetails;

    @Test
    void shouldHaveErrorsPopulatedForAddressFields() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("Street1")
            .addressLine2("addrLine2")
            .build();
        final CicCase cicCase = CicCase.builder().representativeAddress(addressGlobalUK).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = representativeDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(2);
    }

    @Test
    void shouldBeSuccessForValidAddressFields() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("Street1")
            .country("UK")
            .postCode("postcode")
            .build();
        final CicCase cicCase = CicCase.builder().representativeAddress(addressGlobalUK).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = representativeDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isEmpty();
    }
}
