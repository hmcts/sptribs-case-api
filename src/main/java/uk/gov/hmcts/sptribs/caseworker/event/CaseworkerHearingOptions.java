package uk.gov.hmcts.sptribs.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_HEARING_OPTIONS;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerHearingOptions implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private RecordListHelper recordListHelper;

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
                .optional(Listing::getHearingVenues, "venueNotListedOption!=\"VenueNotListed\"")
                .optionalWithLabel(Listing::getVenueNotListedOption, " ")
                .optional(Listing::getRoomAtVenue)
                .optional(Listing::getAddlInstr)
                .optional(Listing::getHearingFormat)
                .optional(Listing::getShortNotice)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        if (isNull(caseData.getListing().getRegionList())) {
            recordListHelper.regionData(caseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final CaseData caseDataBefore = detailsBefore.getData();

        if (isNull(caseData.getListing().getRegionList())) {
            caseData.getListing().setHearingVenues(resetHearingVenuesDynamicList());
            caseData.getListing().setHearingVenuesMessage(null);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        // for edit journey we need to ensure region list is reset if chosen region is changed
        // for edit journey we need to ensure previously entered venue value is retained
        if (!caseData.getListing().getSelectedRegionVal().equals(caseDataBefore.getListing().getSelectedRegionVal())) {
            recordListHelper.populateVenuesData(caseData);
        } else if (!isNull(caseDataBefore.getListing().getHearingVenues())) {
            caseData.getListing().setHearingVenues(caseDataBefore.getListing().getHearingVenues());
            caseData.getListing().getHearingVenues().setValue(caseDataBefore.getListing().getHearingVenues().getValue());
        } else if (isNull(caseData.getListing().getHearingVenues())) {
            recordListHelper.populateVenuesData(caseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();

        if (!caseData.getListing().getVenueNotListedOption().isEmpty()) {
            caseData.getListing().setHearingVenues(resetHearingVenuesDynamicList());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(ReadyToList)
            .build();
    }

    private DynamicList resetHearingVenuesDynamicList() {
        return DynamicList
            .builder()
            .listItems(emptyList())
            .build();
    }
}
