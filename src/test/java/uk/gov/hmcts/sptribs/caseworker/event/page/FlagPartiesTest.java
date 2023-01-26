package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class FlagPartiesTest {

    @InjectMocks
    private FlagParties flagParties;

    @Test
    void shouldReturnErrorsIfNoFlagPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = flagParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorsIfMultipleFlagPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = flagParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }
}
