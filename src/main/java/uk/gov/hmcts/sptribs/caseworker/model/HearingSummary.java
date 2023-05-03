package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.FullPanelHearing;
import uk.gov.hmcts.sptribs.ciccase.model.HearingAttendeesRole;
import uk.gov.hmcts.sptribs.ciccase.model.HearingOutcome;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

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
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
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
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingOutcome outcome;

    @CCD(
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
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>>  recFile;
}
