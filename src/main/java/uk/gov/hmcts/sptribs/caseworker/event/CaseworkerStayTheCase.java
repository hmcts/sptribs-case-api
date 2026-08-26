package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.notification.NotificationConstantProfiles;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseStayedNotification;
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_STAY_THE_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessageFromCorrespondenceParties;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerStayTheCase implements CCDConfig<CaseData, State, UserRole> {

    private final CaseStayedNotification caseStayedNotification;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_STAY_THE_CASE)
            .forStates(CaseManagement, ReadyToList, CaseStayed)
            .name("Stays: Create/edit stay")
            .showSummary()
            .description("Stays: Create/edit stay")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                ST_CIC_JUDGE))
            .page("addStay")
            .pageLabel("Add a Stay to this case")
            .label("LabelAddStay", "")
            .complex(CaseData::getCaseStay)
            .mandatoryWithLabel(CaseStay::getStayReason, "")
            .mandatory(CaseStay::getFlagType, "stayStayReason = \"Other\"")
            .mandatoryWithLabel(CaseStay::getExpirationDate, "")
            .optional(CaseStay::getAdditionalDetail, "");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();
        final CaseStay caseStay = caseData.getCaseStay();

        if (details.getState() != CaseStayed) {
            caseStay.setStayReason(null);
            caseStay.setExpirationDate(null);
            caseStay.setAdditionalDetail(null);
            caseStay.setFlagType(null);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        caseData.getCaseStay().setIsCaseStayed(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseStayed)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        String caseNumber = caseData.getHyphenatedCaseRef();

        NotificationContextRequest request = NotificationContextRequest.builder()
            .caseData(caseData)
            .caseReference(caseNumber)
            .notification(caseStayedNotification)
            .build();

        NotificationContext notificationContext = NotificationConstantProfiles.STAY_THE_CASE.buildContext(
            request);

        notificationDispatcher.sendToCorrespondenceParties(notificationContext);

        if (CollectionUtils.isEmpty(notificationContext.getErrors())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Stay Added to Case %n## %s",
                    generateSimpleMessageFromCorrespondenceParties(notificationContext.getCorrespondenceParties())))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Case stay notification failed %n## %s %n## Please resend the notification.",
                        generateSimpleErrorMessage(notificationContext.getErrors()))
                )
                .build();
        }
    }
}
