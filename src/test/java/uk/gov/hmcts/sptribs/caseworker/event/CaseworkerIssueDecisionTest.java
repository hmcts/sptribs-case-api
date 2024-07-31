package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.DecisionIssuedNotification;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.DecisionTemplateContent;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_DECISION;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueDecisionTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DecisionTemplateContent decisionTemplateContent;

    @InjectMocks
    private CaseWorkerIssueDecision issueDecision;

    @InjectMocks
    private IssueDecisionSelectTemplate issueDecisionSelectTemplate;

    @Mock
    private DecisionIssuedNotification decisionIssuedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        issueDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_DECISION);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(false);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        ReflectionTestUtils.setField(issueDecision, "isWorkAllocationEnabled", true);

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        issueDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);
    }

    @Test
    void shouldSetState() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = new CaseIssueDecision();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename("file.txt").build())
            .documentEmailContent("content")
            .build();
        decision.setDecisionDocument(document);
        caseData.setCaseIssueDecision(decision);
        details.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, beforeDetails);

        //Then
        assertThat(response.getState()).isEqualTo(CaseManagement);
    }

    @Test
    void shouldShowCorrectMessageWhenSubmitted() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = new CaseIssueDecision();
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();
        caseData.setCicCase(cicCase);
        caseData.setCaseIssueDecision(decision);
        caseData.setHyphenatedCaseRef("1234-5678-90");
        details.setData(caseData);

        //When
        SubmittedCallbackResponse response = issueDecision.submitted(details, beforeDetails);

        //Then
        assertThat(response.getConfirmationHeader()).contains("Decision notice issued");
    }

    @Test
    void shouldRenderDocumentWithoutError() {
        //Given
        final CaseIssueDecision caseIssueDecision = new CaseIssueDecision();
        caseIssueDecision.setIssueDecisionTemplate(DecisionTemplate.ELIGIBILITY);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(caseIssueDecision)
            .build();
        caseDetails.setData(caseData);
        Document document = new Document();
        when(caseDataDocumentService.renderDocument(
            anyMap(),
            any(),
            eq(DecisionTemplate.ELIGIBILITY.getId()),
            eq(LanguagePreference.ENGLISH), any(), any()))
            .thenReturn(document);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
        assertThat(caseIssueDecision.getIssueDecisionDraft()).isEqualTo(document);
    }

    @Test
    void shouldReturnMainContentOnMidEvent() {
        //Given
        final CaseIssueDecision caseIssueDecision = new CaseIssueDecision();
        caseIssueDecision.setIssueDecisionTemplate(DecisionTemplate.ELIGIBILITY);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(caseIssueDecision)
            .build();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecisionSelectTemplate.midEvent(caseDetails, caseDetails);

        //Then
        Assertions.assertEquals(DocmosisTemplateConstants.ELIGIBILITY_MAIN_CONTENT, response.getData().getDecisionMainContent());
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getDecisionSignature()).isEmpty();
    }
}
