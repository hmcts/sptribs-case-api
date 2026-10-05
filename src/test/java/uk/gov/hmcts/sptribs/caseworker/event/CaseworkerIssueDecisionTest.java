package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionFooter;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
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
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.DecisionTemplateContent;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.notification.dispatcher.DecisionIssuedNotification;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_DECISION;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueDecisionTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DecisionTemplateContent decisionTemplateContent;

    @InjectMocks
    private IssueDecisionSelectTemplate issueDecisionSelectTemplate;

    @Mock
    private DecisionIssuedNotification decisionIssuedNotification;

    @Mock
    private DocumentsService documentsService;

    @Mock
    private IssueDecisionFooter issueDecisionFooter;

    private final Clock fixedClock = Clock.fixed(
        LocalDate.of(2026, 5, 15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant(),
        ZoneId.systemDefault()
    );

    private CaseworkerIssueDecision issueDecision;

    @BeforeEach
    void setUp() {
        issueDecision = new CaseworkerIssueDecision(
            issueDecisionFooter,
            decisionIssuedNotification,
            fixedClock,
            documentsService
        );
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        issueDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_DECISION);

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
    void shouldSetStateOnAboutToSubmitWhenUploadedFromComputer() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = new CaseIssueDecision();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename("file.txt").build())
            .documentEmailContent("content")
            .build();
        decision.setDecisionDocument(document);
        caseData.setCaseIssueDecision(decision);
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, beforeDetails);

        //Then
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.TRIBUNAL_DIRECTION), eq(CaseDocumentType.DECISION)
        );
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(document.getDocumentLink()), eq(TEST_CASE_ID), eq(DocumentType.TRIBUNAL_DIRECTION), eq(CaseDocumentType.DECISION)
        );

        assertThat(response.getState()).isEqualTo(CaseManagement);
        assertThat(response.getData().getCaseIssueDecision().getDecisionDate()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    @Test
    void shouldSetStateOnAboutToSubmitWhenCreatedFromTemplate() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = new CaseIssueDecision();
        decision.setDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE);
        decision.setIssueDecisionTemplate(DecisionTemplate.ELIGIBILITY);
        final Document document = Document.builder().binaryUrl("url").url("url").filename("file.txt").build();
        decision.setIssueDecisionDraft(document);
        caseData.setCaseIssueDecision(decision);
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, beforeDetails);

        //Then
        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            eq(document), eq(TEST_CASE_ID), eq(DocumentType.TRIBUNAL_DIRECTION), eq(CaseDocumentType.DECISION)
        );

        assertThat(response.getState()).isEqualTo(CaseManagement);
        assertThat(response.getData().getCaseIssueDecision().getDecisionDate()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    @Test
    void shouldSaveRule27DecisionDocumentWhenClosingCase() {
        final Document document = Document.builder().filename("rule-27.pdf").build();
        final CaseIssueDecision decision = CaseIssueDecision.builder()
            .decisionNotice(NoticeOption.CREATE_FROM_TEMPLATE)
            .issueDecisionTemplate(DecisionTemplate.RULE_27)
            .issueDecisionDraft(document)
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(CaseData.builder().caseIssueDecision(decision).build());

        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, new CaseDetails<>());

        assertThat(response.getState()).isEqualTo(CaseClosed);
        assertThat(response.getErrors()).isEmpty();
        assertThat(decision.getDecisionDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        verify(documentsService).buildAndSaveNewDocumentEntity(
            document, TEST_CASE_ID, DocumentType.TRIBUNAL_DIRECTION, CaseDocumentType.DECISION);
    }

    @ParameterizedTest
    @MethodSource("decisionStates")
    void shouldSetStateForDecision(NoticeOption noticeOption, DecisionTemplate template, State expectedState) {
        final CaseIssueDecision decision = CaseIssueDecision.builder()
            .decisionNotice(noticeOption)
            .issueDecisionTemplate(template)
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(CaseData.builder().caseIssueDecision(decision).build());

        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, new CaseDetails<>());

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getState()).isEqualTo(expectedState);
        assertThat(decision.getDecisionDate()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    private static Stream<Arguments> decisionStates() {
        return Stream.of(
            Arguments.of(NoticeOption.CREATE_FROM_TEMPLATE, DecisionTemplate.RULE_27, CaseClosed),
            Arguments.of(NoticeOption.CREATE_FROM_TEMPLATE, DecisionTemplate.STRIKE_OUT_DECISION_NOTICE, CaseClosed),
            Arguments.of(NoticeOption.CREATE_FROM_TEMPLATE, DecisionTemplate.STRIKE_OUT_WARNING, CaseManagement),
            Arguments.of(NoticeOption.CREATE_FROM_TEMPLATE, DecisionTemplate.ELIGIBILITY, CaseManagement),
            Arguments.of(NoticeOption.CREATE_FROM_TEMPLATE, DecisionTemplate.BLANK_DECISION_NOTICE, CaseManagement),
            Arguments.of(NoticeOption.UPLOAD_FROM_COMPUTER, null, CaseManagement),
            Arguments.of(NoticeOption.UPLOAD_FROM_COMPUTER, DecisionTemplate.RULE_27, CaseManagement),
            Arguments.of(NoticeOption.UPLOAD_FROM_COMPUTER, DecisionTemplate.STRIKE_OUT_DECISION_NOTICE, CaseManagement)
        );
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

    @Test
    void shouldStoreErrorsWhenBuildAndSaveNewDocumentEntityThrowsRuntimeException() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = new CaseIssueDecision();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("url").url("url").filename("file.txt").build())
            .documentEmailContent("content")
            .build();
        decision.setDecisionDocument(document);
        caseData.setCaseIssueDecision(decision);
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        DataAccessException testDataAccessException = new DataAccessException("Error saving document entity to database") {};

        doThrow(new DocumentSaveException(testDataAccessException.getMessage(), testDataAccessException))
            .when(documentsService).buildAndSaveNewDocumentEntity(any(), eq(TEST_CASE_ID), eq(DocumentType.TRIBUNAL_DIRECTION),
                eq(CaseDocumentType.DECISION));

        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Error saving document with filename: " + document.getDocumentLink().getFilename());

        verify(documentsService, times(1)).buildAndSaveNewDocumentEntity(
            any(), eq(TEST_CASE_ID), eq(DocumentType.TRIBUNAL_DIRECTION), eq(CaseDocumentType.DECISION)
        );

        document.getDocumentLink().setFilename(null);
        decision.setDecisionDocument(document);
        caseData.setCaseIssueDecision(decision);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> nullFilenameResponse = issueDecision.aboutToSubmit(details, beforeDetails);

        assertThat(nullFilenameResponse.getErrors()).hasSize(1);
        assertThat(nullFilenameResponse.getErrors()).contains("Error saving document with no filename");

        document.getDocumentLink().setFilename("");
        decision.setDecisionDocument(document);
        caseData.setCaseIssueDecision(decision);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> emptyFilenameResponse = issueDecision.aboutToSubmit(details, beforeDetails);

        assertThat(emptyFilenameResponse.getErrors()).hasSize(1);
        assertThat(emptyFilenameResponse.getErrors()).contains("Error saving document with no filename");
    }

    @Test
    void shouldNotSaveDecisionDocumentToDBWhenDecisionDocumentIsNullAndWhenDocumentLinkIsNull() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = new CaseIssueDecision();
        caseData.setCaseIssueDecision(decision);
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        details.setData(caseData);

        issueDecision.aboutToSubmit(details, beforeDetails);

        verifyNoInteractions(documentsService);

        final CICDocument document = CICDocument.builder()
            .documentLink(null)
            .documentEmailContent("content")
            .build();
        decision.setDecisionDocument(document);

        issueDecision.aboutToSubmit(details, beforeDetails);

        verifyNoInteractions(documentsService);
    }
}
