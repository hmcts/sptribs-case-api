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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.CaseIssuedNotification;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
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
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDynamicMultiSelectDocumentListWithXElements;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_CASE;


@ExtendWith(MockitoExtension.class)
class CaseworkerIssueCaseTest {

    @InjectMocks
    private CaseworkerIssueCase caseworkerIssueCase;

    @InjectMocks
    private IssueCaseSelectDocument issueCaseSelectDocument;

    @Mock
    private CaseIssuedNotification caseIssuedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerIssueCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_CASE);
    }

    @Test
    void shouldSuccessfullyIssueTheCase() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(APPLICANT_CIC))
            .notifyPartySubject(Set.of(SUBJECT))
            .notifyPartyRespondent(Set.of(RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        final CaseIssue caseIssue = new CaseIssue();
        caseData.setCaseIssue(caseIssue);
        caseData.setHyphenatedCaseRef("1234-5678-3456");

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerIssueCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        Mockito.doNothing().when(caseIssuedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(caseIssuedNotification).sendToApplicant(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(caseIssuedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(caseIssuedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        SubmittedCallbackResponse submittedResponse = caseworkerIssueCase.submitted(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getCicCase().getNotifyPartyApplicant()).isNotNull();
        assertThat(submittedResponse).isNotNull();
        assertThat(submittedResponse.getConfirmationHeader())
            .contains("# Case issued \n##  This case has now been issued.");
    }


    @Test
    void shouldReturnErrorMessageInSubmittedResponse() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SUBJECT));
        caseData.getCicCase().setNotifyPartyApplicant(Set.of(APPLICANT_CIC));
        caseData.getCicCase().setNotifyPartyRepresentative(Set.of(REPRESENTATIVE));
        caseData.getCicCase().setNotifyPartyRespondent(Set.of(RESPONDENT));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        doThrow(NotificationException.class)
            .when(caseIssuedNotification)
            .sendToSubject(caseData, hyphenatedCaseRef);
        doThrow(NotificationException.class)
            .when(caseIssuedNotification)
            .sendToApplicant(caseData, hyphenatedCaseRef);
        doThrow(NotificationException.class)
            .when(caseIssuedNotification)
            .sendToRepresentative(caseData, hyphenatedCaseRef);
        doThrow(NotificationException.class)
            .when(caseIssuedNotification)
            .sendToRespondent(caseData, hyphenatedCaseRef);

        SubmittedCallbackResponse submittedResponse = caseworkerIssueCase.submitted(caseDetails, caseDetails);

        assertThat(submittedResponse.getConfirmationHeader())
            .isEqualTo("""
                # Issue to respondent notification failed\s
                ## A notification could not be sent to: Subject, Applicant, Representative, Respondent\s
                ## Please resend the notification.""");
    }

    @Test
    void shouldSendErrorOnTooManyDocuments() {
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(APPLICANT_CIC))
            .notifyPartySubject(Set.of(SUBJECT))
            .notifyPartyRespondent(Set.of(RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        final CaseIssue caseIssue = new CaseIssue();
        caseIssue.setDocumentList(getDynamicMultiSelectDocumentListWithXElements(6));
        caseData.setCaseIssue(caseIssue);

        caseData.setHyphenatedCaseRef("1234-5678-3456");

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            issueCaseSelectDocument.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldCreateDocumentList() {
        final CaseData caseData = caseData();
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);

        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .applicantDocumentsUploaded(listValueList)
            .applicantEmailAddress(TEST_APPLICANT_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(APPLICANT_CIC))
            .notifyPartySubject(Set.of(SUBJECT))
            .notifyPartyRespondent(Set.of(RESPONDENT)).build();
        caseData.setCicCase(cicCase);

        caseData.setHyphenatedCaseRef("1234-5678-3456");

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueCase.aboutToStart(updatedCaseDetails);

        assertThat(response).isNotNull();
        DynamicMultiSelectList documentList = response.getData().getCaseIssue().getDocumentList();
        assertThat(documentList).isNotNull();
        assertThat(documentList.getListItems()).hasSize(1);
        assertThat(documentList.getListItems().get(0).getLabel()).isEqualTo("[name.pdf A - Application Form](nulldocuments//binary)");
    }
}
