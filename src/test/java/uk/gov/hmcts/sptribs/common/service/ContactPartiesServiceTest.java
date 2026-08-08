package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.service.CorrespondenceDocumentService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
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

    private List<String> docIds;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        List<ListValue<CaseworkerCICDocument>> docs = TestDataHelper.getCaseworkerCICDocumentList("test.pdf", "test1.doc", "test2.pdf");
        docIds = docs
            .stream().map(ListValue::getValue).map(DocumentUtil::getDocumentUuidFromCaseworkerCICDocument).toList();

        caseData = new CaseData();
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().build());
        caseData.setAllDocManagement(DocumentManagement.builder().caseworkerCICDocument(docs).build());

        DynamicMultiSelectList contactPartiesList = DocumentListUtil.prepareContactPartiesDocumentList(caseData, "test.url");
        contactPartiesList.setValue(contactPartiesList.getListItems());
        caseData.getContactPartiesDocuments().setDocumentList(contactPartiesList);
    }

    @Test
    void shouldLinkCorrespondenceIdsToDocuments() {
        //given
        List<String> correspondenceIds = List.of(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );

        List<Long> documentIds = List.of(1L,2L,3L);

        //when
        when(documentsService.getDocumentIdsByDocumentBinaryUrls(docIds)).thenReturn(documentIds);

        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, correspondenceIds);

        //then
        verify(documentsService, times(1)).getDocumentIdsByDocumentBinaryUrls(docIds);
        correspondenceIds.forEach(correspondenceId ->
            verify(correspondenceDocumentService).saveCorrespondenceDocumentLink(correspondenceId, documentIds));
    }

    @Test
    void shouldContinueLinkingCorrespondenceIdsToDocumentsAfterAFailure() {
        //given
        String correspondenceId1 = UUID.randomUUID().toString();
        String correspondenceId2 = UUID.randomUUID().toString();
        String correspondenceId3 = UUID.randomUUID().toString();
        List<String> correspondenceIds = List.of(
            correspondenceId1,
            correspondenceId2,
            correspondenceId3
        );
        List<Long> documentIds = List.of(1L, 2L, 3L);

        when(documentsService.getDocumentIdsByDocumentBinaryUrls(docIds)).thenReturn(documentIds);

        lenient().doThrow(new CorrespondenceDocumentSaveException("Failed", new RuntimeException()))
            .when(correspondenceDocumentService)
            .saveCorrespondenceDocumentLink(correspondenceId2, documentIds);

        //when
        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, correspondenceIds);

        // then
        verify(documentsService, times(1)).getDocumentIdsByDocumentBinaryUrls(docIds);
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
        List<String> correspondenceIds = List.of(
            UUID.randomUUID().toString()
        );

        doThrow(new DocumentLookupException("Lookup failed", new RuntimeException()))
            .when(documentsService).getDocumentIdsByDocumentBinaryUrls(docIds);

        //when
        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, correspondenceIds);

        //then
        verify(documentsService).getDocumentIdsByDocumentBinaryUrls(docIds);

        verifyNoInteractions(correspondenceDocumentService);
    }

    @Test
    void shouldDoNothingWhenCorrespondenceIdsIsEmpty() {
        //when
        contactPartiesService.linkCorrespondenceIdsToDocuments(caseData, List.of());

        //then
        verifyNoInteractions(documentsService, correspondenceDocumentService);
    }

}
