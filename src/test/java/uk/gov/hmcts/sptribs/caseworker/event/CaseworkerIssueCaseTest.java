package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayEvent;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayResponse;
import uk.gov.hmcts.sptribs.bankholidays.service.BankHolidayService;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseIssuedNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.issueCaseToRespondent;
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

    private static final String DOCUMENT_BINARY_PATH = "documents/%s/binary";

    @InjectMocks
    private IssueCaseSelectDocument issueCaseSelectDocument;

    @Mock
    private BankHolidayService bankHolidayService;

    @Mock
    private CaseIssuedNotification caseIssuedNotification;

    @Mock
    private TaskManagementService taskManagementService;

    private final String bankHolidayUrl = "https://www.gov.uk/bank-holidays/scotland.json";

    private final String baseUrl = "http://localhost:4013/";

    private final BankHolidayResponse testBankHolidayResponse = getBankHolidayResponse();

    private CaseworkerIssueCase caseworkerIssueCase;

    @BeforeEach
    void setUp() {
        caseworkerIssueCase =
            new CaseworkerIssueCase(caseIssuedNotification, bankHolidayService, bankHolidayUrl, baseUrl, taskManagementService);

        Mockito.reset(bankHolidayService, caseIssuedNotification);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerIssueCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_CASE);

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

        when(bankHolidayService.getScottishBankHolidays(anyString())).thenReturn(testBankHolidayResponse);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerIssueCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        doNothing().when(caseIssuedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(caseIssuedNotification).sendToApplicant(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(caseIssuedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(caseIssuedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());

        SubmittedCallbackResponse submittedResponse = caseworkerIssueCase.submitted(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getCicCase().getNotifyPartyApplicant()).isNotNull();
        assertThat(response.getData().getCicCase().getRespondentBundleDueDate()).isNotNull();
        assertThat(submittedResponse).isNotNull();
        assertThat(submittedResponse.getConfirmationHeader())
            .contains("# Case issued \n##  This case has now been issued.");

        verify(taskManagementService).enqueueCompletionTasks(List.of(issueCaseToRespondent), TEST_CASE_ID);
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
                # Issue case notification failed\s
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

        String expectedUrl = this.baseUrl.concat(String.format(DOCUMENT_BINARY_PATH, ""));
        String expectedLabel = String.format("[%s %s](%s)",
            doc.getDocumentLink().getFilename(),
            doc.getDocumentCategory().getLabel(),
            expectedUrl);
        assertThat(documentList).isNotNull();
        assertThat(documentList.getListItems()).hasSize(1);
        assertThat(documentList.getListItems().getFirst().getLabel()).isEqualTo(expectedLabel);
    }

    @ParameterizedTest
    @MethodSource("dueDateScenarios")
    void shouldSetCorrectRespondentBundleDueDate(
            LocalDate calculatedDueDate,
            List<LocalDate> bankHolidayDates,
            LocalDate expectedDueDate,
            String scenario) {

        when(bankHolidayService.getScottishBankHolidays(anyString()))
                .thenReturn(getBankHolidayResponseWithDates(bankHolidayDates));

        Set<LocalDate> bankHolidays = bankHolidayService.getScottishBankHolidays(bankHolidayUrl).getDates();

        LocalDate verifiedDueDate = caseworkerIssueCase.isWorkingDay(calculatedDueDate, bankHolidays)
                ? calculatedDueDate
                : caseworkerIssueCase.getNextWorkingDay(calculatedDueDate, bankHolidays);

        assertThat(verifiedDueDate)
                .as("Scenario: " + scenario)
                .isEqualTo(expectedDueDate);
    }

    @Test
    void shouldCallBankHolidayServiceExactlyOnce() {
        CaseData caseData = CaseData.builder()
                .cicCase(CicCase.builder().build())
                .build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(bankHolidayService.getScottishBankHolidays(anyString())).thenReturn(getBankHolidayResponse());

        caseworkerIssueCase.aboutToSubmit(details, details);

        verify(bankHolidayService, times(1)).getScottishBankHolidays(bankHolidayUrl);
    }

    private BankHolidayResponse getBankHolidayResponse() {
        BankHolidayEvent event1 = new BankHolidayEvent();
        event1.setTitle("New Year's Day");
        event1.setDate(LocalDate.of(2026, 1, 1));
        BankHolidayEvent event2 = new BankHolidayEvent();
        event2.setTitle("2nd January");
        event2.setDate(LocalDate.of(2026, 1, 2));
        BankHolidayEvent event3 = new BankHolidayEvent();
        event3.setTitle("Good Friday");
        event3.setDate(LocalDate.of(2026, 4, 3));
        BankHolidayResponse response = new BankHolidayResponse();
        response.setDivision("Scotland");
        response.setEvents(List.of(event1, event2, event3));

        return response;
    }

    private static Stream<Arguments> dueDateScenarios() {
        return Stream.of(
            Arguments.of(
                LocalDate.of(2026, 3, 4),
                List.of(),
                LocalDate.of(2026, 3, 4),
                "Due date on weekday with no bank holidays - stays as is"
            ),
            Arguments.of(
                LocalDate.of(2026, 3, 7),
                List.of(),
                LocalDate.of(2026, 3, 9),
                "Due date on Saturday - moves to Monday"
            ),
            Arguments.of(
                LocalDate.of(2026, 3, 8),
                List.of(),
                LocalDate.of(2026, 3, 9),
                "Due date on Sunday - moves to Monday"
            ),
            Arguments.of(
                LocalDate.of(2026, 1, 1),
                List.of(
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 2)),
                LocalDate.of(2026, 1, 5),
                "Due date on consecutive bank holidays - Thursday and Friday - moves to Monday"
            ),
            Arguments.of(
                LocalDate.of(2026, 1, 2),
                List.of(LocalDate.of(2026, 1, 5)),
                LocalDate.of(2026, 1, 2),
                "Due date on Friday (working day) - ignore Monday bank holiday"
            ),
            Arguments.of(
                LocalDate.of(2026, 1, 3),
                List.of(LocalDate.of(2026, 1, 5)),
                LocalDate.of(2026, 1, 6),
                "Due date on Saturday with Monday bank holiday - moves to Tuesday"
            ),
            Arguments.of(
                LocalDate.of(2026, 4, 3),
                List.of(
                        LocalDate.of(2026, 4, 3)
                ),
                LocalDate.of(2026, 4, 6),
                "Due date on Good Friday - moves to Monday as Easter Monday is not a bank holiday in Scotland"
            ),
            Arguments.of(
                LocalDate.of(2026, 12, 25),
                List.of(
                    LocalDate.of(2026, 12, 25),
                    LocalDate.of(2026, 12, 28)
                ),
                LocalDate.of(2026, 12, 29),
                "Due date on Christmas Friday - skips weekend and Monday bank holiday to Tuesday"
            )
        );
    }

    private BankHolidayResponse getBankHolidayResponseWithDates(List<LocalDate> dates) {
        BankHolidayResponse response = new BankHolidayResponse();
        response.setDivision("Scotland");
        response.setEvents(dates.stream()
            .map(date -> {
                BankHolidayEvent event = new BankHolidayEvent();
                event.setDate(date);
                event.setTitle("Test Holiday");
                return event;
            })
            .toList());
        return response;
    }
}
