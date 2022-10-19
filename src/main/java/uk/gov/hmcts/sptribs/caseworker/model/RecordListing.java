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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordListing {

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
    private DynamicList regions;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "VenueNotListed",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<VenueNotListed> venueNotListedOption;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingVenueName;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingVenueAddress;

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
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingDateTime;

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
        label = "Hearing, date and start time",
        typeOverride = Collection,
        typeParameterOverride = "HearingDate",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<HearingDate>> additionalHearingDate;

    @JsonIgnore
    public String getSelectedRegionId() {
        return this.getRegions().getValue().getLabel();
    }

    @JsonIgnore
    public String getSelectedVenueName() {
        return this.getHearingVenues().getValue().getLabel();
    }

}
