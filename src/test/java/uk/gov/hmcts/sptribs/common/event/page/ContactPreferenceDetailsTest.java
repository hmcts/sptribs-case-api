package uk.gov.hmcts.sptribs.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ContactPreferenceDetailsTest {

    @InjectMocks
    private ContactPreferenceDetails contactPreferenceDetails;

    @Test
    void shouldSelectContactPreference() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .caseSubcategory(CaseSubcategory.SEXUAL_ABUSE)
            .partiesCIC(Set.of(PartiesCIC.APPLICANT))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPreferenceDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldSelectContactPreferenceFatal() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .caseSubcategory(CaseSubcategory.FATAL)
            .subjectCIC(Set.of(SubjectCIC.SUBJECT))
            .partiesCIC(Set.of(PartiesCIC.SUBJECT)).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPreferenceDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldSelectContactPreferenceMinor() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .caseSubcategory(CaseSubcategory.MINOR)
            .subjectCIC(Set.of(SubjectCIC.SUBJECT))
            .partiesCIC(Set.of(PartiesCIC.SUBJECT)).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPreferenceDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNotNull();
    }
}
