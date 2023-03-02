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
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.event.page.PartiesToContact;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CONTACT_PARTIES;

@ExtendWith(MockitoExtension.class)
class CaseworkerContactPartiesTest {
    @InjectMocks
    private CaseWorkerContactParties caseWorkerContactParties;

    @InjectMocks
    private PartiesToContact partiesToContact;


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
    void shouldDisplayTheCorrectMessageWithCommaSeperatoin() {
        final CaseData caseData = caseData();

        Set<SubjectCIC> sub = new HashSet<>();
        sub.add(SubjectCIC.SUBJECT);
        Set<RepresentativeCIC> rep = new HashSet<>();
        rep.add(RepresentativeCIC.REPRESENTATIVE);
        Set<RespondentCIC> res = new HashSet<>();
        res.add(RespondentCIC.RESPONDENT);

        ContactParties contactParties = ContactParties.builder().subjectContactParties(sub)
            .representativeContactParties(rep).respondent(res).build();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        SubmittedCallbackResponse response =
            caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);


        assertThat(caseData.getContactParties().getSubjectContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRespondent()).hasSize(1);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");
    }

    @Test
    void shouldDisplayTheCorrectMessageWithCommaSeperatoinIfSubjectIsNull() {
        final CaseData caseData = caseData();

        Set<SubjectCIC> sub = new HashSet<>();
        Set<RepresentativeCIC> rep = new HashSet<>();
        rep.add(RepresentativeCIC.REPRESENTATIVE);
        Set<RespondentCIC> res = new HashSet<>();
        res.add(RespondentCIC.RESPONDENT);

        ContactParties contactParties = ContactParties.builder().subjectContactParties(sub)
            .representativeContactParties(rep).respondent(res).build();
        caseData.setContactParties(contactParties);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        SubmittedCallbackResponse response =
            caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(caseData.getContactParties().getSubjectContactParties()).isEmpty();
        assertThat(caseData.getContactParties().getRepresentativeContactParties()).hasSize(1);
        assertThat(caseData.getContactParties().getRespondent()).hasSize(1);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse contactPartiesResponse = caseWorkerContactParties.partiesContacted(updatedCaseDetails, beforeDetails);
        assertThat(contactPartiesResponse).isNotNull();
        assertThat(contactPartiesResponse.getConfirmationHeader()).doesNotContain("Subject");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Representative");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains("Respondent");
        assertThat(contactPartiesResponse.getConfirmationHeader()).contains(",");


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
            partiesToContact.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();


    }


}



