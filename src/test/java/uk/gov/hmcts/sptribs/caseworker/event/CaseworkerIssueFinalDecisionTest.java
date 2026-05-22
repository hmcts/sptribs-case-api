package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionFooter;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionUpload;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseFinalDecisionIssuedNotification;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_FINAL_DECISION;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueFinalDecisionTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private FinalDecisionTemplateContent finalDecisionTemplateContent;

    @Mock
    private CaseFinalDecisionIssuedNotification caseFinalDecisionIssuedNotification;

    @InjectMocks
    private IssueFinalDecisionSelectTemplate issueFinalDecisionSelectTemplate;

    @Mock
    private IssueFinalDecisionFooter issueFinalDecisionFooter;

    @Mock
    private IssueFinalDecisionUpload issueFinalDecisionUpload;

    @Mock
    private HttpServletRequest httpServletRequest;

    private final Clock fixedClock = Clock.fixed(
        LocalDate.of(2026, 5, 15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant(),
        ZoneId.systemDefault()
    );

    private CaseworkerIssueFinalDecision issueFinalDecision;

    @BeforeEach
    void setUp() {
        issueFinalDecision = new CaseworkerIssueFinalDecision(
            issueFinalDecisionFooter,
            httpServletRequest,
            caseDataDocumentService,
            caseFinalDecisionIssuedNotification,
            fixedClock
        );
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        issueFinalDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_FINAL_DECISION);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::isPublishToCamunda)
            .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
            .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
            .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldShowCorrectMessageWhenSubmitted() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        final CicCase cicCase = CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        //When
        SubmittedCallbackResponse response = issueFinalDecision.submitted(details, beforeDetails);

        //Then
        assertThat(response.getConfirmationHeader())
            .contains("Respondent");
    }

    @Test
    void shouldIssueCaseFinalDecision() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        final CaseIssueFinalDecision caseIssueFinalDecision = new CaseIssueFinalDecision();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename("file.txt").build())
            .documentEmailContent("content")
            .build();
        caseIssueFinalDecision.setDocument(document);
        caseData.setCaseIssueFinalDecision(caseIssueFinalDecision);
        details.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecision.aboutToSubmit(details, beforeDetails);

        //Then
        assertThat(response.getState())
            .isEqualTo(CaseClosed);
        assertThat(response.getData().getCaseIssueFinalDecision().getFinalDecisionDate()).isEqualTo(LocalDate.of(2026, 5,15));
    }

    @Test
    void shouldReturnMainContentOnMidEvent() {
        //Given
        final CaseIssueFinalDecision caseIssueFinalDecision = new CaseIssueFinalDecision();
        caseIssueFinalDecision.setDecisionTemplate(DecisionTemplate.ELIGIBILITY);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(caseIssueFinalDecision)
            .build();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecisionSelectTemplate.midEvent(caseDetails, caseDetails);

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
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecision.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getDecisionSignature()).isEmpty();
    }
}
