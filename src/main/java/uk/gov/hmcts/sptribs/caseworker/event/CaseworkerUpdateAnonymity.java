package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ApplyAnonymity;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.notification.dispatcher.AnonymityAppliedNotification;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_UPDATE_ANONYMITY;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerUpdateAnonymity implements CCDConfig<CaseData, State, UserRole> {

    private final ApplyAnonymity applyAnonymity;
    private final AnonymityAppliedNotification anonymityAppliedNotification;
    private final AnonymisationService anonymisationService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_UPDATE_ANONYMITY)
                .forAllStates()
                .name("Update Anonymity")
                .description("Update Anonymity")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_JUDGE, ST_CIC_SENIOR_JUDGE)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        applyAnonymity.addTo(pageBuilder);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        CaseData beforeData = beforeDetails == null ? null : beforeDetails.getData();
        List<String> errors = new ArrayList<>();

        anonymisationService.applyAnonymitySelection(data.getCicCase(), errors, true);

        final ListValue<FlagDetail> mergedAnonymityFlag =
            CaseFlagsUtil.mergeAnonymityFlagsPreserveOriginalId(data, beforeData);

        updateAnonymityCaseFlag(data, mergedAnonymityFlag);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {
        if (details.getState() != null) {
            details.getData().setCaseStatus(details.getState());
        }

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(
            details.getData(),
            beforeDetails == null ? null : beforeDetails.getData(),
            details.getId() == null ? null : details.getId().toString()
        );

        return SubmittedCallbackResponse.builder().build();
    }

    private void updateAnonymityCaseFlag(CaseData data, ListValue<FlagDetail> existingAnonymityFlag) {
        if (YesOrNo.YES.equals(data.getCicCase().getAnonymiseYesOrNo())) {
            if (existingAnonymityFlag != null && existingAnonymityFlag.getValue() != null) {
                existingAnonymityFlag.getValue().setStatus(CaseFlagsUtil.ACTIVE_STATUS);
                existingAnonymityFlag.getValue().setFlagComment("Applied anonymity");
                existingAnonymityFlag.getValue().setDateTimeModified(LocalDateTime.now());
                return;
            }

            FlagDetail flagDetail = FlagDetail.builder()
                .name("RRO (Restricted Reporting Order / Anonymisation)")
                .path(List.of(ListValue.<String>builder().id(UUID.randomUUID().toString()).value("Case").build()))
                .status(CaseFlagsUtil.ACTIVE_STATUS)
                .nameCy("RRO (Gorchymyn Cyfyngiadau Adrodd / Anhysbys)")
                .flagCode(CaseFlagsUtil.ANONYMITY_FLAG_CODE)
                .flagComment("Applied anonymity")
                .dateTimeCreated(LocalDateTime.now())
                .hearingRelevant(YesOrNo.YES)
                .availableExternally(YesOrNo.NO)
                .build();

            CaseFlagsUtil.addFlag(data, flagDetail);
        } else {
            if (existingAnonymityFlag != null && existingAnonymityFlag.getValue() != null) {
                existingAnonymityFlag.getValue().setStatus("Inactive");
                existingAnonymityFlag.getValue().setDateTimeModified(LocalDateTime.now());
            }
        }
    }
}
