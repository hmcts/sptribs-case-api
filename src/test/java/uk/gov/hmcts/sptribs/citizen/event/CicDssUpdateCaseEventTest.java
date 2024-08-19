package uk.gov.hmcts.sptribs.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssMessage;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.common.notification.DssUpdateCaseSubmissionNotification;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.DSS_TRIBUNAL_FORM;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CicDssUpdateCaseEventTest {

    @Mock
    private DssUpdateCaseSubmissionNotification dssUpdateCaseSubmissionNotification;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private CicDssUpdateCaseEvent cicDssUpdateCaseEvent;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        cicDssUpdateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(false);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(false);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        ReflectionTestUtils.setField(cicDssUpdateCaseEvent, "isWorkAllocationEnabled", true);

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        cicDssUpdateCaseEvent.configure(configBuilder);

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
    void shouldAddDocumentsAndMessagesToCaseDataInAboutToSubmitCallback() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final DssMessage message = DssMessage.builder()
            .message("new doc")
            .build();
        final List<ListValue<DssMessage>> messages = new ArrayList<>();
        messages.add(new ListValue<>("1", message));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .messages(messages)
            .dssCaseData(getDssCaseData())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());
        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(2);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();
    }

    @Test
    void shouldAddDocumentsOnlyToCaseDataInAboutToSubmitCallbackIfAdditionalInfoFieldIsEmpty() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final DssMessage message = DssMessage.builder()
            .message("new doc")
            .build();
        final List<ListValue<DssMessage>> messages = new ArrayList<>();
        messages.add(new ListValue<>("1", message));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .messages(messages)
            .dssCaseData(getDssCaseData())
            .build();
        caseData.getDssCaseData().setAdditionalInformation("");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(1);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();
    }

    @Test
    void shouldAddDocumentsOnlyToCaseDataInAboutToSubmitCallbackIfAdditionalInfoFieldIsNull() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final DssMessage message = DssMessage.builder()
            .message("new doc")
            .build();
        final List<ListValue<DssMessage>> messages = new ArrayList<>();
        messages.add(new ListValue<>("1", message));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .messages(messages)
            .dssCaseData(getDssCaseData())
            .build();
        caseData.getDssCaseData().setAdditionalInformation(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(2).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(1);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();
    }

    @Test
    void shouldCreateNewApplicantDocumentsUploadedAndMessagesListIfEmptyInAboutToSubmitCallback() {
        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(new ArrayList<>())
                    .build()
            )
            .messages(new ArrayList<>())
            .dssCaseData(getDssCaseData())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(2);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().get(1).getValue().getDate())
            .isEqualTo(LocalDate.now());
        assertThat(response.getData().getMessages()).isNotEmpty();
        assertThat(response.getData().getMessages()).hasSize(1);
        assertThat(response.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response.getData().getDssCaseData().getAdditionalInformation()).isNull();
    }

    @Test
    void shouldSendEmailNotifications() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        SubmittedCallbackResponse response = cicDssUpdateCaseEvent.submitted(details, details);

        assertThat(response.getConfirmationHeader())
            .isEqualTo("# CIC Dss Update Case Event Email notifications sent");

        verify(dssUpdateCaseSubmissionNotification).sendToApplicant(
            details.getData(),
            TEST_CASE_ID.toString()
        );
        verify(dssUpdateCaseSubmissionNotification).sendToTribunal(
            details.getData(),
            TEST_CASE_ID.toString()
        );
    }

    @Test
    void shouldCatchErrorIfSendEmailNotificationFails() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        doThrow(NotificationException.class)
            .when(dssUpdateCaseSubmissionNotification)
            .sendToApplicant(details.getData(), TEST_CASE_ID.toString());

        SubmittedCallbackResponse response = cicDssUpdateCaseEvent.submitted(details, details);

        assertThat(response.getConfirmationHeader())
            .contains("# CIC Dss Update Case Event Email notification failed %n## Please resend the notification");
    }

    private DssCaseData getDssCaseData() {
        EdgeCaseDocument doc1 = new EdgeCaseDocument();
        doc1.setDocumentLink(
            Document.builder()
                .filename("doc1.pdf")
                .binaryUrl("doc1.pdf/binary")
                .categoryId("test category")
                .build()
        );
        doc1.setComment("this doc is relevant to the case");
        EdgeCaseDocument doc2 = new EdgeCaseDocument();
        doc2.setDocumentLink(
            Document.builder()
                .filename("doc2.pdf")
                .binaryUrl("doc2.pdf/binary")
                .categoryId("test category")
                .build()
        );
        doc2.setComment("this doc is also relevant to the case");
        final List<ListValue<EdgeCaseDocument>> dssCaseDataOtherInfoDocuments = List.of(
            new ListValue<>("1", doc1),
            new ListValue<>("2", doc2)
        );

        return DssCaseData.builder()
            .additionalInformation("some additional info")
            .otherInfoDocuments(dssCaseDataOtherInfoDocuments)
            .build();
    }
}
