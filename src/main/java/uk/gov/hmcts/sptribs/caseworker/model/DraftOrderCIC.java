package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftOrderCIC {


    @CCD(
        label = "Template",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "OrderTemplate"
    )
    private OrderTemplate anOrderTemplate;




//    @CCD(
//        label = "Main Content CIC1_Eligibility",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC1_Eligibility\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC1Eligibility;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC2_Quantum\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC2Quantum;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC3_Rule_27\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC3Rule27;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC4_Blank_Decision_Notice_1\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC4BlankDecisionNotice1;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC6_General_Directions\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC6GeneralDirections;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC7_ME_Dmi_Reports\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC7MEDmiReports;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC8_ME_Joint_Instruction\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC8MEJointInstruction;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC10_Strike_Out_Warning\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC10StrikeOutWarning;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC11_Strike_Out_Decision_Notice\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC11StrikeOutDecisionNotice;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC12_Decision_Annex\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC12DecisionAnnex;
//
//    @CCD(
//        label = "Main Content",
//        hint = "Amend content as required",
//        showCondition = "anOrderTemplate = \"CIC13_Pro_Forma_Summons\"",
//        typeOverride = TextArea,
//        access = {CaseworkerAndSuperUserAccess.class}
//    )
//    private String mainContentForCIC13ProFormaSummons;


}
