package uk.gov.hmcts.sptribs.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_HEARING_OPTIONS;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

public class CaseworkerHearingOptions implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private LocationService locationService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_HEARING_OPTIONS)
            .forStates(CaseManagement, ReadyToList)
            .name("Case: Hearing Options")
            .description("Case: Hearing Options")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER))
            .page("hearingOptionsRegionData", this::midEvent)
            .pageLabel("Region Data")
            .complex(CaseData::getListing)
                .optional(Listing::getRegionList)
            .done()
            .page("hearingOptionsHearingDetails")
            .pageLabel("Hearing Details")
            .complex(CaseData::getListing)
                .optional(Listing::getHearingVenues)
                .optional(Listing::getVenueNotListedOption)
                .optional(Listing::getRoomAtVenue)
                .optional(Listing::getAddlInstr)
                .optional(Listing::getHearingFormat)
                .optional(Listing::getShortNotice)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        final DynamicList regionList = locationService.getAllRegions();
        caseData.getListing().setRegionList(regionList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        String selectedRegion = caseData.getListing().getSelectedRegionVal();
        String regionId = locationService.getRegionId(selectedRegion);

        if (regionId != null) {
            DynamicList hearingVenueList = locationService.getHearingVenuesByRegion(regionId);
            caseData.getListing().setHearingVenues(hearingVenueList);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(ReadyToList)
            .build();
    }
}
