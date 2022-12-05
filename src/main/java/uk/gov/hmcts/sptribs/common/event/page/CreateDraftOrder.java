package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.CollectionUtils.isEmpty;



@Slf4j
@Component
public class CreateDraftOrder implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createDraftOrder")
            .pageLabel("Create order")
            .label("createDraftOrder", "Draft to be created")
           // .complex(CaseData::getDraftOrderCIC)
            //.mandatory(DraftOrderCIC::getOrderTemplate, "")
            .complex(CaseData::getCicCase)
          //  .mandatory(CicCase::getOrderTemplates)
           // .optional(CicCase::getMainContentForCIC1Eligibility)
            .optional(CicCase::getDraftOrderCICList)
            .label("edit", "<hr>" + "\n<h3>Header</h3>" + "\n<h4>First tier tribunal Health lists</h4>\n\n"
                + "<h3>IN THE MATTER OF THE NATIONAL HEALTH SERVICES (PERFORMERS LISTS)(ENGLAND) REGULATIONS 2013</h2>\n\n"
                + "&lt; &lt; CaseNumber &gt; &gt; \n"
                + "\nBETWEEN\n"
                + "\n&lt; &lt; SubjectName &gt; &gt; \n"
                + "\nApplicant\n"
                + "\n<RepresentativeName>"
                + "\nRespondent<hr>"
                + "\n<h3>Main content</h3>\n\n ")
//            .optional(DraftOrderCIC::getMainContentForCIC1Eligibility, "draftOrderTemplate = \"CIC1_Eligibility\"")
//            .optional(DraftOrderCIC::getMainContentForCIC2Quantum, "draftOrderTemplate = \"CIC2_Quantum\"")
//            .optional(DraftOrderCIC::getMainContentForCIC3Rule27, "draftOrderTemplate = \"CIC3_Rule_27\"")
//            .optional(DraftOrderCIC::getMainContentForCIC4BlankDecisionNotice1, "draftOrderTemplate = \"CIC4_Blank_Decision_Notice_1\"")
//            .optional(DraftOrderCIC::getMainContentForCIC6GeneralDirections, "draftOrderTemplate = \"CIC6_General_Directions\"")
//            .optional(DraftOrderCIC::getMainContentForCIC7MEDmiReports, "draftOrderTemplate = \"CIC7_ME_Dmi_Reports\"")
//            .optional(DraftOrderCIC::getMainContentForCIC8MEJointInstruction, "draftOrderTemplate = \"CIC8_ME_Joint_Instruction\"")
//            .optional(DraftOrderCIC::getMainContentForCIC10StrikeOutWarning, "draftOrderTemplate = \"CIC10_Strike_Out_Warning\"")
//            .optional(DraftOrderCIC::getMainContentForCIC11StrikeOutDecisionNotice,
//                "draftOrderTemplate = \"CIC11_Strike_Out_Decision_Notice\"")
//            .optional(DraftOrderCIC::getMainContentForCIC12DecisionAnnex, "draftOrderTemplate = \"CIC12_Decision_Annex\"")
//            .optional(DraftOrderCIC::getMainContentForCIC13ProFormaSummons, "draftOrderTemplate = \"CIC13_Pro_Forma_Summons\"")
            .label("footer", "<h2>Footer</h2>\n First-tier Tribunal (Health,Education and Social Care)\n\n"
                + "Date Issued &lt; &lt;  SaveDate &gt; &gt;")
            .done();
    }


//    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
//        final CaseDetails<CaseData, State> details,
//        final CaseDetails<CaseData, State> beforeDetails
//    ) {
//        var caseData = details.getData();
//        var draftOrder = caseData.getDraftOrderCIC();
//
//        if (isEmpty(caseData.getDraftOrderCICList())) {
//            List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();
//
//            var listValue = ListValue
//                .<DraftOrderCIC>builder()
//                .id("1")
//                .value(draftOrder)
//                .build();
//
//            listValues.add(listValue);
//
//            caseData.setDraftOrderCICList(listValues);
//        } else {
//            AtomicInteger listValueIndex = new AtomicInteger(0);
//            var listValue = ListValue
//                .<DraftOrderCIC>builder()
//                .value(draftOrder)
//                .build();
//
//            caseData.getDraftOrderCICList().add(0, listValue); // always add new note as first element so that it is displayed on top
//
//            caseData.getDraftOrderCICList().forEach(
//                caseDraftOrderCic -> caseDraftOrderCic.setId(String.valueOf(listValueIndex.incrementAndGet()))
//            );
//
//        }
//
//        caseData.setDraftOrderCIC(null);
//        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
//            .data(caseData)
//            .state(details.getState())
//            .build();
//
//    }

}
