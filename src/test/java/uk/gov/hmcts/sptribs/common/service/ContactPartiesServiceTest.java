package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.document.service.CorrespondenceDocumentService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactPartiesServiceTest {

    @InjectMocks
    private ContactPartiesService contactPartiesService;
    @Mock
    private DocumentsService documentsService;
    @Mock
    private CorrespondenceDocumentService correspondenceDocumentService;

    @Test
    void shouldLinkCorrespondenceIdsToDocuments() {
        //given
        CaseData caseData = new CaseData();
        Map<String, String> uploadedDocuments = Map.of(
            "doc1", "yes",
            "doc2", "yes"
        );

        List<String> correspondenceIds = new ArrayList<>();
        String correspondenceId1 = UUID.randomUUID().toString();
        String correspondenceId2 = UUID.randomUUID().toString();
        String correspondenceId3 = UUID.randomUUID().toString();
        correspondenceIds.add(correspondenceId1);
        correspondenceIds.add(correspondenceId2);
        correspondenceIds.add(correspondenceId3);

        List<Long> documentIds = List.of(1L,2L);

        when(documentsService.getDocumentsViaSentByContactParties(caseData, uploadedDocuments)).thenReturn(documentIds);

        //when
        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, uploadedDocuments, correspondenceIds);

        //then
        verify(correspondenceDocumentService).saveCorrespondenceDocumentLink(correspondenceId1, documentIds);
        verify(correspondenceDocumentService).saveCorrespondenceDocumentLink(correspondenceId2, documentIds);
        verify(correspondenceDocumentService).saveCorrespondenceDocumentLink(correspondenceId3, documentIds);

    }

    @Test
    void shouldContinueLinkingCorrespondenceIdsToDocumentsAfterAFailure() {
        //given
        CaseData caseData = new CaseData();
        Map<String, String> uploadedDocuments = Map.of(
            "doc1", "yes",
            "doc2", "yes"
        );

        String correspondenceId1 = UUID.randomUUID().toString();
        String correspondenceId2 = UUID.randomUUID().toString();
        String correspondenceId3 = UUID.randomUUID().toString();

        List<String> correspondenceIds = List.of(
            correspondenceId1,
            correspondenceId2,
            correspondenceId3
        );

        List<Long> documentIds = List.of(1L, 2L);

        when(documentsService.getDocumentsViaSentByContactParties(caseData, uploadedDocuments))
            .thenReturn(documentIds);

        doNothing().when(correspondenceDocumentService).saveCorrespondenceDocumentLink(correspondenceId1, documentIds);

        doThrow(new CorrespondenceDocumentSaveException("Failed", new RuntimeException()))
            .when(correspondenceDocumentService)
            .saveCorrespondenceDocumentLink(correspondenceId2, documentIds);

        doNothing().when(correspondenceDocumentService).saveCorrespondenceDocumentLink(correspondenceId3, documentIds);

        //when
        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, uploadedDocuments, correspondenceIds);

        // then
        verify(correspondenceDocumentService)
            .saveCorrespondenceDocumentLink(correspondenceId1, documentIds);

        verify(correspondenceDocumentService)
            .saveCorrespondenceDocumentLink(correspondenceId2, documentIds);

        verify(correspondenceDocumentService)
            .saveCorrespondenceDocumentLink(correspondenceId3, documentIds);

    }

    @Test
    void shouldCatchDocumentLookupException() {
        //given
        CaseData caseData = new CaseData();
        Map<String, String> uploadedDocuments = Map.of(
            "doc1", "yes"
        );

        List<String> correspondenceIds = List.of(
            UUID.randomUUID().toString()
        );

        doThrow(new DocumentLookupException("Lookup failed", new RuntimeException()))
            .when(documentsService).getDocumentsViaSentByContactParties(caseData, uploadedDocuments);

        //when
        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, uploadedDocuments, correspondenceIds);

        //then
        verify(documentsService).getDocumentsViaSentByContactParties(caseData, uploadedDocuments);

        verifyNoInteractions(correspondenceDocumentService);
    }

}
