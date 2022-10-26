package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

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

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_RECORD_LISTING)
            .forStates(CaseManagement)
            .name("Record listing")
            .showSummary()
            .description("Record listing")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

        addHearingTypeAndFormat(pageBuilder);
        addRemoteHearingInfo(pageBuilder);
        addOtherInformation(pageBuilder);
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

    private void addHearingTypeAndFormat(PageBuilder pageBuilder) {
        pageBuilder.page("hearingTypeAndFormat")
            .label("hearingTypeAndFormatObj", "<h1>Hearing type and format</h1>")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
            .done();
    }

    private void addRemoteHearingInfo(PageBuilder pageBuilder) {
        pageBuilder.page("remoteHearingInformation")
            .label("remoteHearingInfoObj", "<h1>Remote hearing information</h1>")
            .complex(CaseData::getRecordListing)
            .optional(RecordListing::getVideoCallLink)
            .optional(RecordListing::getConferenceCallNumber)
            .done();
    }

    private void addOtherInformation(PageBuilder pageBuilder) {
        pageBuilder.page("otherInformation")
            .label("otherInformationObj", "<h1>Other information</h1>")
            .complex(CaseData::getRecordListing)
            .label("otherInfoLabel",
                "\nEnter any other important information about this hearing. This may include any reasonable adjustments that need to be made, or details" +
                    "\n of anyone who should be excluded from attending this hearing.\n")
            .optional(RecordListing::getImportantInfoDetails)
            .done();
    }
}
