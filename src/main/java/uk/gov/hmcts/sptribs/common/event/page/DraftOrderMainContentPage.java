package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderMainContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class DraftOrderMainContentPage implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("mainContent")
            .pageLabel("Edit order")
            .label("createDraftOrder", "Edit order")

            .label("edit1", "<hr>" + "\n<h3>Header</h3>" + "\nThe header will be automatically generated ,"
                +"you can preview this in pdf on the next screen.\n\n"
                +"<hr>")
            .label("edit2",
                "<h3>Main content</h3>\n\n "
                +"Enter text in the box below.This will be added into the centre "
                +"of the generated order document")
            .complex(CaseData::getDraftOrderMainContentCIC)
            .optional(DraftOrderMainContentCIC::getMainContentForCIC1Eligibility,"cicCaseAnOrderTemplates = \"CIC1_Eligibility\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC2Quantum,"cicCaseAnOrderTemplates = \"CIC2_Quantum\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC3Rule27,"cicCaseAnOrderTemplates = \"CIC3_Rule_27\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC4BlankDecisionNotice1,"cicCaseAnOrderTemplates = \"CIC4_Blank_Decision_Notice_1\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC6GeneralDirections,"cicCaseAnOrderTemplates = \"CIC6_General_Directions\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC7MEDmiReports,"cicCaseAnOrderTemplates = \"CIC7_ME_Dmi_Reports\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC8MEJointInstruction,"cicCaseAnOrderTemplates = \"CIC8_ME_Joint_Instruction\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC10StrikeOutWarning,"cicCaseAnOrderTemplates = \"CIC10_Strike_Out_Warning\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC11StrikeOutDecisionNotice,"cicCaseAnOrderTemplates = \"CIC11_Strike_Out_Decision_Notice\"")
            .optional(DraftOrderMainContentCIC::getMainContentForCIC12DecisionAnnex,"cicCaseAnOrderTemplates = \"CIC12_Decision_Annex\"")
//            .label("footer", "<h2>Footer</h2>\n First-tier Tribunal (Health,Education and Social Care)\n\n"
//                + "Date Issued &lt; &lt;  SaveDate &gt; &gt;")
            .done();
    }


}
