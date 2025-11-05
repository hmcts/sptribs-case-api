package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.event.page.RespondentPartiesToContact;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.TribunalCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_CONTACT_PARTY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class RespondentContactPartiesTest {

    @InjectMocks
    private RespondentContactParties respondentContactParties;

    @InjectMocks
    private RespondentPartiesToContact respondentPartiesToContact;

    @Mock
    private ContactPartiesNotification contactPartiesNotification;

    @Mock
    private ContactPartiesSelectDocument contactPartiesSelectDocument;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        respondentContactParties.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(RESPONDENT_CONTACT_PARTIES);
    }

    @Test
    void shouldSuccessfullySaveResContactParties() {
        //Given
        final CaseData caseData = caseData();
        caseData.getContactParties().setSubjectContactParties(Set.of(SubjectCIC.SUBJECT));
        caseData.getContactParties().setApplicantContactParties(Set.of(ApplicantCIC.APPLICANT_CIC));
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .applicantContactParties(Set.of(ApplicantCIC.APPLICANT_CIC))
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .tribunal(Set.of(TribunalCIC.TRIBUNAL)).build();
        caseData.setContactParties(contactParties);

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(resContactPartiesResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyMoveToNextPage() {
        //Given
        final CaseData caseData = caseData();
        CicCase cicCase = CicCase.builder().contactPartiesCIC(Set.of(ContactPartiesCIC.SUBJECTTOCONTACT)).build();
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
    }

    @Test
    void shouldNotSuccessfullyMoveToNextPageWithError() {
        //Given
        Set<SubjectCIC> sub = new HashSet<>();
        Set<ApplicantCIC> app = new HashSet<>();
        Set<RepresentativeCIC> rep = new HashSet<>();
        Set<TribunalCIC> tri = new HashSet<>();

        ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(sub)
            .applicantContactParties(app)
            .representativeContactParties(rep)
            .tribunal(tri).build();
        final CaseData caseData = caseData();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(caseData.getContactParties().getSubjectContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getApplicantContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getTribunal()).isEmpty();
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(SELECT_AT_LEAST_ONE_CONTACT_PARTY);

        //When
        SubmittedCallbackResponse contactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Applicant");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Tribunal");
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparation() {
        //Given
        Set<SubjectCIC> sub = new HashSet<>();
        sub.add(SubjectCIC.SUBJECT);
        Set<ApplicantCIC> app = new HashSet<>();
        app.add(ApplicantCIC.APPLICANT_CIC);
        Set<RepresentativeCIC> rep = new HashSet<>();
        rep.add(RepresentativeCIC.REPRESENTATIVE);
        Set<TribunalCIC> tri = new HashSet<>();
        tri.add(TribunalCIC.TRIBUNAL);

        ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(sub)
            .applicantContactParties(app)
            .representativeContactParties(rep)
            .tribunal(tri).build();
        final CaseData caseData = caseData();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        SubmittedCallbackResponse response =
            respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(caseData.getContactParties().getSubjectContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getApplicantContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getTribunal()).hasSize(1);
        assertThat(response).isNotNull();

        //When
        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(resContactPartiesResponse).isNotNull();
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Applicant");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Tribunal");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains(",");
    }

    @Test
    void shouldSuccessfullyMoveToNextPageWithOutError() {
        //Given
        final CaseData caseData = caseData();
        CicCase cicCase = CicCase.builder().contactPartiesCIC(Set.of()).build();
        cicCase.setRepresentativeFullName("www");
        caseData.setCicCase(cicCase);
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

}
