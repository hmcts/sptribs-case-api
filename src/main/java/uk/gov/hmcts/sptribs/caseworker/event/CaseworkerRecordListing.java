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
import uk.gov.hmcts.sptribs.common.event.page.HearingTypeAndFormat;
import uk.gov.hmcts.sptribs.common.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.hearingvenue.LocationService;
import uk.gov.hmcts.sptribs.payment.PaymentService;
import uk.gov.hmcts.sptribs.payment.model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private static final CcdPageConfiguration hearingTypeAndFormat = new HearingTypeAndFormat();

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

        hearingTypeAndFormat.addTo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {

        var caseData = details.getData();

        /*OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent("other", "enforcement", "BailiffServeDoc");

        caseData.getRecordListing().setHearingVenues(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(String.valueOf(orderSummary.getPaymentTotal())).build())
            .listItems(List.of(DynamicListElement.builder().label(orderSummary.getPaymentTotal()).code(UUID.randomUUID()).build()))
            .build());*/

        //populatePbaDynamicList(orderSummary, caseData.getRecordListing());

        caseData.getRecordListing().setRegions(locationService.getRegionList());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker record listing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Listing record created"))
            .build();
    }
}
