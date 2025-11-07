package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftOrderFooterTest {

    @InjectMocks
    private DraftOrderFooter draftOrderFooter;

    @Mock
    private OrderService orderService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

    @Test
    void shouldAddFooterSuccessfully() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        Calendar calendar = Calendar.getInstance();
        String date = simpleDateFormat.format(calendar.getTime());

        when(orderService.generateOrderFile(caseDetails.getData(), caseDetails.getId(), date)).thenReturn(caseData);

        var response = draftOrderFooter.midEvent(caseDetails, caseDetails);
        assertThat(response.getData()).isEqualTo(caseData);
        verify(orderService).generateOrderFile(caseDetails.getData(), caseDetails.getId(), date);

    }
}
