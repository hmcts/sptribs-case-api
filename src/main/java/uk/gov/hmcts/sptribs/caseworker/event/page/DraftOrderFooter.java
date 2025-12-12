package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DraftOrderFooter implements CcdPageConfiguration {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

    private final OrderService orderService;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("draftOrderDocumentFooter", this::midEvent)
            .pageLabel("Document footer")
            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditionsNew())
            .label("draftOrderDocFooter",
                """
                    Order Signature

                    Confirm the Role and Surname of the person who made this order - this will be added to the bottom of the generated \
                    order notice. E.g. 'Tribunal Judge Farrelly'""")
            .complex(CaseData::getDraftOrderContentCIC)
            .mandatory(DraftOrderContentCIC::getOrderSignature)
            .done();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        Calendar cal = Calendar.getInstance();
        String date = simpleDateFormat.format(cal.getTime());
        final CaseData caseData = orderService.generateOrderFile(details.getData(), details.getId(), date);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
