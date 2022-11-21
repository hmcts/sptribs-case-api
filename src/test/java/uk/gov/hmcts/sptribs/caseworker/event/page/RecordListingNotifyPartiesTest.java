package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.elasticsearch.core.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RecordListingNotifyPartiesTest {

    @InjectMocks
    private RecordListingNotifyParties recordListingNotifyParties;

    @Test
    void shouldReturnErrorsIfCaseDataIsNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = recordListingNotifyParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder().build());
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = recordListingNotifyParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldBeSuccessfulIfNotificationPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = new CicCase();
        cicCase.setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        cicCase.setNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE));
        cicCase.setNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT));
        final CaseData caseData = CaseData.builder().cicCase(cicCase).build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = recordListingNotifyParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }
}
