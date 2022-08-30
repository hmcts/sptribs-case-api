package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

@ExtendWith(MockitoExtension.class)
class DocumentIdProviderTest {

    @InjectMocks
    private DocumentIdProvider documentIdProvider;

    @Test
    void shouldProvideDocumentId() {
        //When
        final String documentId = documentIdProvider.documentId();
        //Then
        assertThat(documentId, matchesPattern("([a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12})"));
    }
}
