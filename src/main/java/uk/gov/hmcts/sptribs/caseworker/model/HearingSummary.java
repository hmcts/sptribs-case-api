package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.FullPanelHearing;
import uk.gov.hmcts.sptribs.ciccase.model.HearingAttendeesRole;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingOutcome;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class HearingSummary {

    @CCD(
        label = "Hearing type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingType hearingType;

    @CCD(
        label = "Hearing format",
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingFormat",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingFormat hearingFormat;

    @CCD(
        label = "Which judge heard the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList judge;

    @CCD(
        label = "panel member Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList panelMemberName;

    @CCD(
        label = "Was it a full panel hearing?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FullPanelHearing fullPanelHearing;

    @CCD(
        label = "Panel member and Role",
        typeOverride = Collection,
        typeParameterOverride = "PanelMember",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<PanelMember>> panelMemberList;

    @CCD(
        label = "Who attended the hearing?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "HearingAttendeesRole",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<HearingAttendeesRole> hearingAttendeesRole;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Who was this other attendee?"
    )
    private String otherAttendee;

    @CCD(
        label = "What type of decision was given at the hearing?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingOutcome hearingOutcome;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String subjectName;

    @CCD(
        label = "Where can the recording be found?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String hearingRecordingDescription;

    @CCD(
        label = "Upload the recording of the hearing",
        typeOverride = Collection,
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CICDocument>> recordingUpload;
}
