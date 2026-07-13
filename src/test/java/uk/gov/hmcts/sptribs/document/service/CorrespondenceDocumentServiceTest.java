package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceDocumentRepository;

@ExtendWith(MockitoExtension.class)
class CorrespondenceDocumentServiceTest {

    @InjectMocks
    private CorrespondenceDocumentService correspondenceDocumentService;
    @Mock
    private CorrespondenceDocumentRepository correspondenceDocumentRepository;

    @Test
    void shouldSaveCorrespondenceDocumentLink() {

    }

    @Test
    void shouldThrowCorrespondenceDocumentSaveExceptionAfterDBError(){

    }

}
