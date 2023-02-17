package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingSession;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordListing {

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String selectedRegionId;


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
        label = "Hearing venue",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList hearingVenues;

    @CCD(
        label = "Region",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList regionList;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "VenueNotListed",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<VenueNotListed> venueNotListedOption;

    @CCD(
        label = "Venue name and address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingVenueNameAndAddress;

    @CCD(
        label = "Venue Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String readOnlyHearingVenueName;

    @CCD(
        label = "Room at venue",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String roomAtVenue;

    @CCD(
        label = "Additional instructions and directions",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String addlInstr;

    @CCD(
        label = "Hearing Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;

    @CCD(
        label = "Start time (24hr format)",
        regex = "^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingTime;

    @CCD(
        label = "Session",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingSession session;

    @CCD(
        label = "Will this hearing take place across a number of days?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo numberOfDays;

    @CCD(
        label = "Additional Hearing date",
        typeOverride = Collection,
        typeParameterOverride = "HearingDate",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<HearingDate>> additionalHearingDate;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingVenuesMessage;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String regionsMessage;

    @CCD(
        label = "Video call link",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String videoCallLink;

    @CCD(
        label = "Conference call number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String conferenceCallNumber;

    @CCD(
        label = "Other important information ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String importantInfoDetails;

    @CCD(
        label = "Why is this listing record being changed?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String recordListingChangeReason;


    @JsonIgnore
    public String getSelectedRegionVal() {
        return this.getRegionList() != null ? this.getRegionList().getValue().getLabel() : null;
    }

    @JsonIgnore
    public String getSelectedVenue() {
        return this.getHearingVenues() != null ? this.getHearingVenues().getValue().getLabel() : null;
    }

}
