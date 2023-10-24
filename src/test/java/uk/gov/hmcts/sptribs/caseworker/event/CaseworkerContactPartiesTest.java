package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.event.page.PartiesToContact;
import uk.gov.hmcts.sptribs.common.notification.ContactPartiesNotification;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_APPLICANT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CONTACT_PARTIES;

@ExtendWith(MockitoExtension.class)
class CaseworkerContactPartiesTest {
    @InjectMocks
    private CaseWorkerContactParties caseWorkerContactParties;

    @InjectMocks
    private PartiesToContact partiesToContact;

    @Mock
    private ContactPartiesNotification contactPartiesNotification;

    @InjectMocks
    private ContactPartiesSelectDocument contactPartiesSelectDocument;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseWorkerContactParties.setContactPartiesEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerContactParties.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CONTACT_PARTIES);
    }

    @Test
    void shouldNotConfigureContactPartiesIfFeatureFlagFalse() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerContactParties.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_CONTACT_PARTIES);
    }

    @Test
    void shouldSuccessfullySaveContactParties() {
        //Given
        final CaseData caseData = caseData();
        caseData.getContactParties().setSubjectContactParties(Set.of(SubjectCIC.SUBJECT));
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.getContactParties().setRespondent(Set.of(RespondentCIC.RESPONDENT));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        ContactParties contactParties = ContactParties.builder().subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE)).respondent(Set.of(RespondentCIC.RESPONDENT)).build();
        caseData.setContactParties(contactParties);

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerContactParties.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(caseData.getContactParties().getSubjectContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRespondent()).hasSize(1);
        assertThat(response).isNotNull();

        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();


    }

    @Test
    void shouldSuccessfullyMoveToNextPage() {
        final CaseData caseData = caseData();
        CicCase cicCase = CicCase.builder().contactPartiesCIC(Set.of(ContactPartiesCIC.SUBJECTTOCONTACT)).build();
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();


    }


    @Test
    void shouldNotSuccessfullyMoveToNextPageWithError() {
        final CaseData caseData = caseData();

        Set<SubjectCIC> sub = new HashSet<>();
        Set<RepresentativeCIC> rep = new HashSet<>();
        Set<RespondentCIC> res = new HashSet<>();

        ContactParties contactParties = ContactParties.builder().subjectContactParties(sub)
            .representativeContactParties(rep).respondent(res).build();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);


        assertThat(caseData.getContactParties().getSubjectContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getRespondent()).isEmpty();
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).hasSize(1);

        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Respondent");


    }


    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparation() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        SubmittedCallbackResponse response =
            caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);

        assertThat(caseData.getCicCase().getNotifyPartySubject()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyApplicant()).hasSize(1);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeparationIfSubjectIsNull() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        Mockito.doNothing().when(contactPartiesNotification).sendToApplicant(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(contactPartiesNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(contactPartiesNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());

        SubmittedCallbackResponse response =
            caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).hasSize(1);
        assertThat(caseData.getCicCase().getNotifyPartyApplicant()).hasSize(1);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Applicant");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");
    }


    @Test
    void shouldSuccessfullyMoveToNextPageWithOutError() {
        final CaseData caseData = caseData();

        CicCase cicCase = CicCase.builder().caseSubcategory(CaseSubcategory.MEDICAL_REOPENING)
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC)).contactPartiesCIC(Set.of()).build();
        cicCase.setRepresentativeFullName("www");
        caseData.setCicCase(cicCase);
        caseData.getContactParties().setRepresentativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();


    }


}



