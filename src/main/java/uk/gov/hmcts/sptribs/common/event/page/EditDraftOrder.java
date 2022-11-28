package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class EditDraftOrder implements CcdPageConfiguration {



    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editDraftOrder",this::aboutToSubmit)
            .pageLabel("Edit order")
            .label("editableDraft", "Draft to be edited")
            .complex(CaseData::getDraftOrderCIC)
            .mandatory(DraftOrderCIC::getOrderTemplate, "")
            .label("edit", "<hr>" + "\n<h3>Header</h3>" + "\n<h4>First tier tribunal Health lists</h4>\n\n"
                + "<h3>IN THE MATTER OF THE NATIONAL HEALTH SERVICES (PERFORMERS LISTS)(ENGLAND) REGULATIONS 2013</h2>\n\n"
                + "&lt; &lt; CaseNumber &gt; &gt; \n"
                + "\nBETWEEN\n"
                + "\n&lt; &lt; SubjectName &gt; &gt; \n"
                + "\nApplicant\n"
                + "\n<RepresentativeName>"
                + "\nRespondent<hr>"
                + "\n<h3>Main content</h3>\n\n ")
            .optional(DraftOrderCIC::getMainContentForGeneralDirections, "draftOrderTemplate = \"GeneralDirections\"")
            .optional(DraftOrderCIC::getMainContentForDmiReports, "draftOrderTemplate = \"Medical Evidence - DMI Reports\"")
            .label("footer", "<h2>Footer</h2>\n First-tier Tribunal (Health,Education and Social Care)\n\n"
                + "Date Issued &lt; &lt;  SaveDate &gt; &gt;")


            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        var draftOrder = caseData.getDraftOrderCIC();

        if (isEmpty(caseData.getDraftOrderCICList())) {
            List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<DraftOrderCIC>builder()
                .id("1")
                .value(draftOrder)
                .build();

            listValues.add(listValue);

            caseData.setDraftOrderCICList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<DraftOrderCIC>builder()
                .value(draftOrder)
                .build();

            caseData.getDraftOrderCICList().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getDraftOrderCICList().forEach(
                caseDraftOrderCic -> caseDraftOrderCic.setId(String.valueOf(listValueIndex.incrementAndGet()))
            );

        }

        caseData.setDraftOrderCIC(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();

    }

    public SubmittedCallbackResponse draftCreated(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("When you have finished drafting this order,you can send it to parties in this case.")
            .build();
    }
}
