package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.RespondentPartiesToContact;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.TribunalCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        ContactParties contactParties = ContactParties.builder().subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE)).tribunal(Set.of(TribunalCIC.TRIBUNAL)).build();
        caseData.setContactParties(contactParties);

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentContactParties.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(caseData.getContactParties().getSubjectContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getTribunal()).hasSize(1);
        assertThat(response).isNotNull();

        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(resContactPartiesResponse).isNotNull();

    }


    @Test
    void shouldSuccessfullyMoveToNextPage() {
        final CaseData caseData = caseData();
        CicCase cicCase = CicCase.builder().contactPartiesCIC(Set.of(ContactPartiesCIC.SUBJECTTOCONTACT)).build();
        caseData.getContactParties().setTribunal(Set.of(TribunalCIC.TRIBUNAL));
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();


    }

    @Test
    void shouldNotSuccessfullyMoveToNextPageWithError() {
        final CaseData caseData = caseData();

        Set<SubjectCIC> sub = new HashSet<>();
        Set<RepresentativeCIC> rep = new HashSet<>();
        Set<TribunalCIC> tri = new HashSet<>();

        ContactParties contactParties = ContactParties.builder().subjectContactParties(sub)
            .representativeContactParties(rep).tribunal(tri).build();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);


        assertThat(caseData.getContactParties().getSubjectContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getTribunal()).isEmpty();
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);

        SubmittedCallbackResponse contactPartiesResponse = respondentContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Tribunal");


    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparation() {
        final CaseData caseData = caseData();

        Set<SubjectCIC> sub = new HashSet<>();
        sub.add(SubjectCIC.SUBJECT);
        Set<RepresentativeCIC> rep = new HashSet<>();
        rep.add(RepresentativeCIC.REPRESENTATIVE);
        Set<TribunalCIC> tri = new HashSet<>();
        tri.add(TribunalCIC.TRIBUNAL);

        ContactParties contactParties = ContactParties.builder().subjectContactParties(sub)
            .representativeContactParties(rep).tribunal(tri).build();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        SubmittedCallbackResponse response =
            respondentContactParties.partiesContacted(updatedCaseDetails, beforeDetails);


        assertThat(caseData.getContactParties().getSubjectContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getTribunal()).hasSize(1);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse resContactPartiesResponse = respondentContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(resContactPartiesResponse).isNotNull();
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains("Tribunal");
        assertThat(resContactPartiesResponse.getConfirmationHeader()).contains(",");
    }


    @Test
    void shouldSuccessfullyMoveToNextPageWithOutError() {
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
        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentPartiesToContact.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();


    }


}
