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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.StatementSelectParty;
import uk.gov.hmcts.sptribs.caseworker.event.page.StatementUploadDocument;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.statement.service.StatementPersistenceException;
import uk.gov.hmcts.sptribs.statement.service.StatementService;
import uk.gov.hmcts.sptribs.statement.service.StatementValidationException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_UPLOAD_STATEMENT;

@ExtendWith(MockitoExtension.class)
class CaseworkerUploadStatementTest {

    @InjectMocks
    private CaseworkerUploadStatement caseworkerUploadStatement;

    @Mock
    private StatementSelectParty statementSelectParty;

    @Mock
    private StatementUploadDocument statementUploadDocument;

    @Mock
    private StatementService statementService;

    @Test
    void shouldConfigureUploadStatementEvent() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUploadStatement.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPLOAD_STATEMENT);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::isPublishToCamunda)
            .contains(true);
    }

    @Test
    void shouldPopulatePartyListOnAboutToStartWhenRepresentativePresent() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .representativeFullName("Representative Person")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadStatement.aboutToStart(caseDetails);

        assertThat(response.getData().getStatementUpload().getStatementParty()).isNotNull();
        assertThat(response.getData().getStatementUpload().getStatementDocument()).isNull();
        assertThat(response.getData().getStatementUpload().getStatementParty().getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Applicant", "Respondent", "Representative");
    }

    @Test
    void shouldPopulatePartyListOnAboutToStartWithoutRepresentative() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .representativeFullName("")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadStatement.aboutToStart(caseDetails);

        assertThat(response.getData().getStatementUpload().getStatementParty().getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Applicant", "Respondent");
    }

    @Test
    void shouldPopulateOnlyRepresentativeWhenOtherPartiesMissing() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("")
            .respondentName("")
            .representativeFullName("Representative Person")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadStatement.aboutToStart(caseDetails);

        assertThat(response.getData().getStatementUpload().getStatementParty().getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Representative");
    }

    @Test
    void shouldSaveStatementAndResetTransientFieldsOnAboutToSubmit() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1234567890123456L);

        caseworkerUploadStatement.aboutToStart(caseDetails);
        caseData.getStatementUpload().getStatementParty()
            .setValue(caseData.getStatementUpload().getStatementParty().getListItems().getFirst());
        caseData.getStatementUpload().setStatementDocument(Document.builder()
            .url("http://dm/documents/abc")
            .binaryUrl("http://dm/documents/abc/binary")
            .filename("statement.pdf")
            .build());

        doNothing().when(statementService).saveStatement(eq(1234567890123456L), eq(caseData));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadStatement.aboutToSubmit(caseDetails, new CaseDetails<>());

        verify(statementService).saveStatement(eq(1234567890123456L), eq(caseData));

        assertThat(response.getData().getStatementUpload().getStatementParty()).isNull();
        assertThat(response.getData().getStatementUpload().getStatementDocument()).isNull();
    }

    @Test
    void shouldReturnSubmittedConfirmation() {
        SubmittedCallbackResponse response =
            caseworkerUploadStatement.submitted(new CaseDetails<>(), new CaseDetails<>());

        assertThat(response.getConfirmationHeader()).isEqualTo("# Statement uploaded");
    }

    @Test
    void shouldReturnErrorWhenSelectedPartyIsInvalidForCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .representativeFullName("")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1234567890123456L);

        caseworkerUploadStatement.aboutToStart(caseDetails);
        caseData.getStatementUpload().getStatementParty().setValue(
            caseData.getStatementUpload().getStatementParty().getListItems().stream()
                .filter(element -> "Applicant".equals(element.getLabel()))
                .findFirst()
                .orElseThrow()
        );
        caseData.getStatementUpload().getStatementParty().getValue().setLabel("Representative");

        caseData.getStatementUpload().setStatementDocument(Document.builder()
            .url("http://dm/documents/abc")
            .binaryUrl("http://dm/documents/abc/binary")
            .filename("statement.pdf")
            .build());

        doThrow(new StatementValidationException("Please select a valid party for the statement"))
            .when(statementService).saveStatement(eq(1234567890123456L), eq(caseData));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadStatement.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).contains("Please select a valid party for the statement");
        verify(statementService).saveStatement(eq(1234567890123456L), eq(caseData));
        assertThat(response.getData().getStatementUpload().getStatementParty()).isNotNull();
    }

    @Test
    void shouldReturnErrorWhenDocumentTypeIsInvalid() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1234567890123456L);

        caseworkerUploadStatement.aboutToStart(caseDetails);
        caseData.getStatementUpload().getStatementParty()
            .setValue(caseData.getStatementUpload().getStatementParty().getListItems().getFirst());
        caseData.getStatementUpload().setStatementDocument(Document.builder()
            .url("http://dm/documents/abc")
            .binaryUrl("http://dm/documents/abc/binary")
            .filename("statement.exe")
            .build());

        doThrow(new StatementValidationException("Please upload valid document"))
            .when(statementService).saveStatement(eq(1234567890123456L), eq(caseData));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadStatement.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).contains("Please upload valid document");
        verify(statementService).saveStatement(eq(1234567890123456L), eq(caseData));
    }

    @Test
    void shouldReturnGenericErrorWhenStatementPersistenceFails() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1234567890123456L);

        caseworkerUploadStatement.aboutToStart(caseDetails);
        caseData.getStatementUpload().getStatementParty()
            .setValue(caseData.getStatementUpload().getStatementParty().getListItems().getFirst());
        caseData.getStatementUpload().setStatementDocument(Document.builder()
            .url("http://dm/documents/abc")
            .binaryUrl("http://dm/documents/abc/binary")
            .filename("statement.pdf")
            .build());

        doThrow(new StatementPersistenceException("Failed to save statement", new RuntimeException("db error")))
            .when(statementService).saveStatement(eq(1234567890123456L), eq(caseData));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadStatement.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).contains("Unable to save statement right now. Please try again.");
        assertThat(response.getData().getStatementUpload().getStatementParty()).isNotNull();
    }

    @Test
    void shouldReturnGenericErrorWhenUnexpectedRuntimeExceptionOccurs() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .build());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1234567890123456L);

        caseworkerUploadStatement.aboutToStart(caseDetails);
        caseData.getStatementUpload().getStatementParty()
            .setValue(caseData.getStatementUpload().getStatementParty().getListItems().getFirst());
        caseData.getStatementUpload().setStatementDocument(Document.builder()
            .url("http://dm/documents/abc")
            .binaryUrl("http://dm/documents/abc/binary")
            .filename("statement.pdf")
            .build());

        doThrow(new RuntimeException("unexpected"))
            .when(statementService).saveStatement(eq(1234567890123456L), eq(caseData));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadStatement.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).contains("Unable to save statement right now. Please try again.");
        assertThat(response.getData().getStatementUpload().getStatementParty()).isNotNull();
    }
}
