package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.hearingvenue.LocationService;
import uk.gov.hmcts.sptribs.payment.PaymentService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerRecordListing implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_RECORD_LISTING = "caseworker-record-listing";

    private static final CcdPageConfiguration hearingVenues = new HearingVenues();

    @Autowired
    private LocationService locationService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_RECORD_LISTING)
            .forStates(CaseManagement)
            .name("Record listing")
            .showSummary()
            .description("Record listing")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

        addHearingTypeAndFormat(pageBuilder);
        hearingVenues.addTo(pageBuilder);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {

        var caseData = details.getData();

        // TODO: Santoshini: Remove below working Payment service refdata call
        /*OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent("other", "enforcement", "BailiffServeDoc");

        caseData.getRecordListing().setRegions(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(String.valueOf(orderSummary.getPaymentTotal())).build())
            .listItems(List.of(DynamicListElement.builder().label(orderSummary.getPaymentTotal()).code(UUID.randomUUID()).build()))
            .build());*/

        caseData.getRecordListing().setRegions(locationService.getRegionList());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    @SneakyThrows
    private AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker record listing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .build();
    }

    private SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Listing record created"))
            .build();
    }

    private void addHearingTypeAndFormat(PageBuilder pageBuilder) {
        pageBuilder.page("hearingTypeAndFormat", this::midEvent)
            .label("hearingTypeAndFormatObj", "<h1>Hearing type and format</h1>")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final List<String> errors = new ArrayList<>();
        /*String regionId = caseData.getRecordListing().getSelectedRegionId();

        if(null != regionId) {
            caseData.getRecordListing().setHearingVenues(locationService.getHearingVenuesByRegion(regionId));
        }*/

        OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent("other", "enforcement", "BailiffServeDoc");

        caseData.getRecordListing().setHearingVenues(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(String.valueOf(orderSummary.getPaymentTotal())).build())
            .listItems(List.of(DynamicListElement.builder().label(orderSummary.getPaymentTotal()).code(UUID.randomUUID()).build()))
            .build());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }
}
