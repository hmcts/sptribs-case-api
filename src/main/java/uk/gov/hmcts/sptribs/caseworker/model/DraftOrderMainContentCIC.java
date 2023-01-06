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

import java.awt.*;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftOrderMainContentCIC {

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC1Eligibility;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC2Quantum;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC3Rule27;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC4BlankDecisionNotice1;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC6GeneralDirections;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC7MEDmiReports;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC8MEJointInstruction;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC10StrikeOutWarning;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC11StrikeOutDecisionNotice;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC12DecisionAnnex;

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContentForCIC13ProFormaSummons;



    @CCD(
        label="<h2>Footer</h2>\n\n The footer will be automatically generated."
        + "You can preview this in the pdf document on the next screen",
       // typeOverride = Text,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private Label footer;


}
