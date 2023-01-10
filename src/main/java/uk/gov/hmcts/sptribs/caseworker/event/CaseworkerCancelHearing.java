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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CancelHearingDateSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.CancelHearingReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.Arrays;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.getEmailMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.getPostMessage;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCancelHearing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration hearingDateSelect = new CancelHearingDateSelect();
    private static final CcdPageConfiguration reasonSelect = new CancelHearingReasonSelect();

    @Autowired
    private HearingService hearingService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = cancelStart(configBuilder);
        hearingDateSelect.addTo(pageBuilder);
        reasonSelect.addTo(pageBuilder);
    }

    public PageBuilder cancelStart(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CANCEL_HEARING)
            .forStates(AwaitingHearing)
            .name("Cancel hearing")
            .description("Cancel hearing")
            .showEventNotes()
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::hearingCancelled)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList hearingDateDynamicList = hearingService.getHearingDateDynamicList(details);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker case cancel hearing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        var state = details.getState();

        DynamicListElement selectedHearing = caseData.getCicCase().getHearingList().getValue();

        if (null != selectedHearing) {
            state = CaseManagement;
            caseData.setRecordListing(null);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse hearingCancelled(CaseDetails<CaseData, State> details,
                                                      CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        var cicCase = details.getData().getCicCase();
        final StringBuilder emailMessage = getEmailMessage(cicCase, data.getCicCase().getHearingNotificationParties());

        final StringBuilder postMessage = getPostMessage(cicCase, data.getCicCase().getHearingNotificationParties());

        String message = "";
        if (null != postMessage && null != emailMessage) {
            message = format("#  Hearing cancelled   %n" + " %s  %n  %s", emailMessage.substring(0, emailMessage.length() - 2),
                postMessage.substring(0, postMessage.length() - 2));
        } else if (null != emailMessage) {
            message = format("#  Hearing cancelled  %n" + " %s ", emailMessage.substring(0, emailMessage.length() - 2));

        } else if (null != postMessage) {
            message = format("#  Hearing cancelled  %n" + " %s ", postMessage.substring(0, postMessage.length() - 2));
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(message)
            .build();
    }

    public static ListValue<HearingDate> getSelectedHearing(String selectedDraft, RecordListing recordListing) {
        String[] values = (selectedDraft != null) ? Arrays.stream(selectedDraft.split(SPACE))
            .map(String::trim)
            .toArray(String[]::new) : null;
        if (null != values) {
            for (ListValue<HearingDate> date : recordListing.getAdditionalHearingDate()) {
                if (values[0].equals(date.getValue().getHearingVenueDate().toString())
                    && values[1].equals(date.getValue().getHearingVenueTime())) {
                    return date;
                }
            }
        }
        return null;
    }

}
