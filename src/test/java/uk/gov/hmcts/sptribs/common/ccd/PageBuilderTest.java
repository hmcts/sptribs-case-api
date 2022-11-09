package uk.gov.hmcts.sptribs.common.ccd;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

@ExtendWith(MockitoExtension.class)
class PageBuilderTest {

    @Mock
    private Event.EventBuilder<CaseData, UserRole, State> eventBuilder;

    @Mock
    private MidEvent midEvent;

    private FieldCollection.FieldCollectionBuilder fieldsBuilder;

    @InjectMocks
    private PageBuilder pageBuilder;

    @BeforeEach
    void setUp() {
        pageBuilder = new PageBuilder(eventBuilder);
        fieldsBuilder = FieldCollection.FieldCollectionBuilder.builder(null, null, null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnPage() {
        //when
        Mockito.when(eventBuilder.fields()).thenReturn(fieldsBuilder);

        //then
        Assertions.assertThat(pageBuilder.page("id1")).isEqualTo(fieldsBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnPageWithMidEvent() {
        //when
        Mockito.when(eventBuilder.fields()).thenReturn(fieldsBuilder);

        //then
        Assertions.assertThat(pageBuilder.page("id1", midEvent)).isEqualTo(fieldsBuilder);
    }
}
