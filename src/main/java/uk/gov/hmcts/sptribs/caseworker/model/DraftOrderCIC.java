package uk.gov.hmcts.sptribs.caseworker.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class DraftOrderCIC {


    @CCD(
        label = "Template",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "OrderTemplate"
    )
    private OrderTemplate orderTemplate;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC1Eligibility;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC2Quantum;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC3Rule27;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC4BlankDecisionNotice1;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC6GeneralDirections;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC7MEDmiReports;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC8MEJointInstruction;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC10StrikeOutWarning;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC11StrikeOutDecisionNotice;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC12DecisionAnnex;

    @CCD(
        label = "Main Content",
        hint = "Amend content as required",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC13ProFormaSummons;





}
