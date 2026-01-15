package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactPartiesSelectDocumentTest {

    private static final String SYSTEM_AUTH = "system-auth-token";
    private static final String SERVICE_AUTH = "service-auth-token";

    @InjectMocks
    private ContactPartiesSelectDocument contactPartiesSelectDocument;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User systemUser;

    @Nested
    class RequireStubbing {
        @BeforeEach
        void setUp() {
            systemUser = mock(User.class);
            when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
            when(systemUser.getAuthToken()).thenReturn(SYSTEM_AUTH);
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH);
        }

        @Test
        void midEventIsSuccessful() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            List<DynamicListElement> selection = IntStream.range(0, 10)
                .mapToObj(i -> DynamicListElement.builder()
                    .code(UUID.randomUUID())
                    .label("Document " + i)
                    .build())
                .toList();
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());
            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
            assertTrue(response.getErrors().isEmpty());
        }

        @Test
        void midEventReturnsErrorWhenMaxDocumentsExceeded() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            List<DynamicListElement> selection = IntStream.range(0, 11)
                .mapToObj(i -> DynamicListElement.builder()
                    .code(UUID.randomUUID())
                    .label("Document " + i)
                    .build())
                .toList();
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());
            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).contains("Select up to 10 documents");
        }



        @Test
        void midEventReturnsErrorWhenDocumentSizeExceedsLimit() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            UUID documentId = UUID.randomUUID();
            String label = "[Large Document](http://example/documents/" + documentId + ")";

            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            DynamicListElement element = DynamicListElement.builder()
                .code(documentId)
                .label(label)
                .build();
            List<DynamicListElement> selection = List.of(element);
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());

            Document oversizedDocument = new Document();
            oversizedDocument.size = 3_000_000;
            when(caseDocumentClientApi.getDocument(SYSTEM_AUTH, SERVICE_AUTH, documentId))
                .thenReturn(ResponseEntity.ok(oversizedDocument));

            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
            String displayName = label.substring(label.indexOf('[') + 1, label.indexOf(']'));
            assertThat(response.getErrors()).containsExactly("Unable to proceed because " + displayName + " is larger than 2MB");
        }

        @Test
        void midEventDoesNotReturnErrorWhenDocumentSizeWithinLimit() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            UUID documentId = UUID.randomUUID();
            String label = "[Small Document](http://example/documents/" + documentId + ")";

            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            DynamicListElement element = DynamicListElement.builder()
                .code(documentId)
                .label(label)
                .build();
            List<DynamicListElement> selection = List.of(element);
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());

            Document withinLimitDocument = new Document();
            withinLimitDocument.size = 1_500_000;
            when(caseDocumentClientApi.getDocument(SYSTEM_AUTH, SERVICE_AUTH, documentId))
                .thenReturn(ResponseEntity.ok(withinLimitDocument));

            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void midEventUsesDocumentIdFromLabelWithBinarySuffix() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            UUID documentId = UUID.randomUUID();
            String label = "[Binary Document](http://example/documents/" + documentId + "/binary)";

            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            DynamicListElement element = DynamicListElement.builder()
                .code(documentId)
                .label(label)
                .build();
            List<DynamicListElement> selection = List.of(element);
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());

            Document withinLimitDocument = new Document();
            withinLimitDocument.size = 1_500_000;
            when(caseDocumentClientApi.getDocument(SYSTEM_AUTH, SERVICE_AUTH, documentId))
                .thenReturn(ResponseEntity.ok(withinLimitDocument));

            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);

            ArgumentCaptor<UUID> docIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(caseDocumentClientApi).getDocument(eq(SYSTEM_AUTH), eq(SERVICE_AUTH), docIdCaptor.capture());
            assertThat(docIdCaptor.getValue()).isEqualTo(documentId);
        }

        @Test
        void midEventUsesDocumentIdFromLabelWithoutBinarySuffix() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            UUID documentId = UUID.randomUUID();
            String label = "[Plain Document](http://example/documents/" + documentId + ")";

            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            DynamicListElement element = DynamicListElement.builder()
                .code(documentId)
                .label(label)
                .build();
            List<DynamicListElement> selection = List.of(element);
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());

            Document withinLimitDocument = new Document();
            withinLimitDocument.size = 1_500_000;
            when(caseDocumentClientApi.getDocument(SYSTEM_AUTH, SERVICE_AUTH, documentId))
                .thenReturn(ResponseEntity.ok(withinLimitDocument));

            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);

            ArgumentCaptor<UUID> docIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(caseDocumentClientApi).getDocument(eq(SYSTEM_AUTH), eq(SERVICE_AUTH), docIdCaptor.capture());
            assertThat(docIdCaptor.getValue()).isEqualTo(documentId);
        }

        @Test
        void midEventTreatsMissingDocumentBodyAsOversized() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            UUID documentId = UUID.randomUUID();
            String label = "[Unknown Document](http://example/documents/" + documentId + ")";

            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            DynamicListElement element = DynamicListElement.builder()
                .code(documentId)
                .label(label)
                .build();
            List<DynamicListElement> selection = List.of(element);
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());

            when(caseDocumentClientApi.getDocument(SYSTEM_AUTH, SERVICE_AUTH, documentId))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());

            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
            String displayName = label.substring(label.indexOf('[') + 1, label.indexOf(']'));
            assertThat(response.getErrors()).containsExactly("Unable to proceed because " + displayName + " is larger than 2MB");
        }

        @Test
        void midEventThrowsExceptionWhenDocumentRetrievalFails() {
            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            UUID documentId = UUID.randomUUID();

            ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
            DynamicListElement element = DynamicListElement.builder()
                .code(documentId)
                .label("[Missing Document](http://example/documents/" + documentId + ")")
                .build();
            List<DynamicListElement> selection = List.of(element);
            contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
                .value(selection)
                .listItems(selection)
                .build());

            when(caseDocumentClientApi.getDocument(SYSTEM_AUTH, SERVICE_AUTH, documentId))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

            final CaseData caseData = CaseData.builder()
                .contactPartiesDocuments(contactPartiesDocuments)
                .build();
            caseDetails.setData(caseData);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactPartiesSelectDocument.midEvent(caseDetails, caseDetails));

            assertThat(exception.getMessage()).isEqualTo("Failed to retrieve document with id " + documentId);
        }

    }

    @Test
    void midEventReturnsWithNoDocumentsSelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
        contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder().build());
        final CaseData caseData = CaseData.builder()
            .contactPartiesDocuments(contactPartiesDocuments)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).isEmpty();
    }

}
