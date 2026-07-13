package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.document.service.CorrespondenceDocumentService;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

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

    }

    @Test
    void shouldContinueLinkingCorrespondenceIdsToDocumentsAfterAFailure() {

    }

    @Test
    void shouldCatchDocumentLookupException() {

    }



}
