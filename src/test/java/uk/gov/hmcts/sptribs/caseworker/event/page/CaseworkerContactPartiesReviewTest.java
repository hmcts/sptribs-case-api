package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseworkerContactPartiesReviewTest {

    @InjectMocks
    private CaseworkerContactPartiesReview caseworkerContactPartiesReview;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PageBuilder pageBuilder;

    @Test
    void shouldAddCaseworkerContactPartiesReviewPage() {
        caseworkerContactPartiesReview.addTo(pageBuilder);

        verify(pageBuilder).page("contactPartiesReview");
    }
}
