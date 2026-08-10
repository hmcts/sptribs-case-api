package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactPartiesReviewPageTest {

    @Mock
    private PageBuilder pageBuilder;

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureContactPartiesReviewPage() {
        ContactPartiesReview page = new ContactPartiesReview();
        FieldCollection.FieldCollectionBuilder fieldsBuilder =
            mock(FieldCollection.FieldCollectionBuilder.class, RETURNS_SELF);

        when(pageBuilder.page("contactPartiesReview")).thenReturn(fieldsBuilder);
        when(fieldsBuilder.readonlyWithLabel(any(), any())).thenReturn(fieldsBuilder);

        page.addTo(pageBuilder);

        verify(pageBuilder).page("contactPartiesReview");
        verify(fieldsBuilder).pageLabel("Check your answers");
        verify(fieldsBuilder).label("contactPartiesReviewDocumentsChange",
            "[Change documents](/callbacks/mid-event?page=contactPartiesSelectDocument&eventId=contact-parties)");
        verify(fieldsBuilder).label("contactPartiesReviewRecipientsChange",
            "[Change contact parties](/callbacks/mid-event?page=partiesToContact&eventId=contact-parties)");
        verify(fieldsBuilder).readonlyWithLabel(any(), eq("Selected documents"));
        verify(fieldsBuilder).readonlyWithLabel(any(), eq("Recipients"));
        verify(fieldsBuilder).readonlyWithLabel(any(), eq("Message"));
    }
}
