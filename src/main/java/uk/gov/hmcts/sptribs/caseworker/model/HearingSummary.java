package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.AdjournmentReasons;
import uk.gov.hmcts.sptribs.ciccase.model.FullPanelHearing;
import uk.gov.hmcts.sptribs.ciccase.model.HearingAttendeesRole;
import uk.gov.hmcts.sptribs.ciccase.model.HearingOutcome;
import uk.gov.hmcts.sptribs.ciccase.model.PanelComposition;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.SecondPanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.ThirdPanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CollectionDefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_1;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_2;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_3;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingSummary {

    @CCD(
        label = "Which judge heard the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList judge;

    @CCD(
        label = "Information about Judges stored in dynamic list",
        typeOverride = Collection,
        typeParameterOverride = "Judge",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<Judge>> judgeList;

    @CCD(
        label = "panel member Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList names;

    @CCD(
        label = "Was it a full panel hearing?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FullPanelHearing isFullPanel;

    @CCD(
        label = "Panel member and Role",
        typeOverride = Collection,
        typeParameterOverride = "PanelMember",
        access = {CaseworkerWithCAAAccess.class, CollectionDefaultAccess.class}
    )
    private List<ListValue<PanelMember>> memberList;

    @CCD(
        label = "Who attended the hearing?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "HearingAttendeesRole",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<HearingAttendeesRole> roles;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Who was this other attendee?"
    )
    private String others;

    @CCD(
        label = "What type of decision was given at the hearing?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingOutcome",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingOutcome outcome;

    @CCD(
        label = "Why was the hearing adjourned?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "AdjournmentReasons",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AdjournmentReasons adjournmentReasons;

    @CCD(
        label = "Enter any other important information about this adjournment",
        typeOverride = TextArea
    )
    private String otherDetailsOfAdjournment;

    @CCD(
        label = "Subject Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String subjectName;

    @CCD(
        label = "Where can the recording be found?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String recDesc;

    @CCD(
        label = "Upload the recording of the hearing",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {CollectionDefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> recFile;

    @CCD(
        label = "Upload the recording of the hearing",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocumentUpload",
        access = {CollectionDefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CaseworkerCICDocumentUpload>> recFileUpload;

    @CCD(
        label = "Panel 1",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String panel1;

    @CCD(
        label = "Panel 2",
        typeOverride = FixedList,
        typeParameterOverride = "SecondPanelMember",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private SecondPanelMember panel2;

    @CCD(
        label = "Panel 3",
        typeOverride = FixedList,
        typeParameterOverride = "ThirdPanelMember",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ThirdPanelMember panel3;

    @CCD(
        label = "Specialisms or reserved panel member information",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String panelMemberInformation;

    @CCD(
        label = "Panel Composition",
        typeOverride = FixedList,
        typeParameterOverride = "PanelComposition",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private PanelComposition panelComposition;

    @JsonIgnore
    public void populatePanelComposition() {
        if (this.getPanel2() != null && this.getPanel3() != null) {
            this.setPanelComposition(PANEL_3);
        } else if (this.getPanel2() != null || this.getPanel3() != null) {
            this.setPanelComposition(PANEL_2);
        } else {
            this.setPanelComposition(PANEL_1);
        }
    }
}
