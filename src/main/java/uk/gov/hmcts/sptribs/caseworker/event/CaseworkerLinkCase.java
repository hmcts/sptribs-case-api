package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.notification.NotificationConstantProfiles;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseLinkedNotification;
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_LINK_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessageFromCorrespondenceParties;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class CaseworkerLinkCase implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALWAYS_HIDE = "LinkedCasesComponentLauncher = \"DONOTSHOW\"";

    private final CaseLinkedNotification caseLinkedNotification;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_LINK_CASE)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome, ReadyToList)
            .name("Link cases")
            .description("To link related cases")
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                ST_CIC_SENIOR_JUDGE,
                ST_CIC_JUDGE))
            .page("createCaseLink")
            .pageLabel("Case Link")
            .optional(CaseData::getCaseLinks, ALWAYS_HIDE, null, true)
            .optional(CaseData::getLinkedCasesComponentLauncher,
                null, null, null, null, "#ARGUMENT(CREATE,LinkedCases)");
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        String caseNumber = caseData.getHyphenatedCaseRef();

        NotificationContextRequest request = NotificationContextRequest.builder()
            .caseData(caseData)
            .caseReference(caseNumber)
            .notification(caseLinkedNotification)
            .build();

        NotificationContext notificationContext = NotificationConstantProfiles.LINK_CASE.buildContext(
            request);

        notificationDispatcher.sendToCorrespondenceParties(notificationContext);

        if (isEmpty(notificationContext.getErrors())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case Link created %n## %s",
                    generateSimpleMessageFromCorrespondenceParties(notificationContext.getCorrespondenceParties())))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Case link notification failed %n## %s %n## Please resend the notification.",
                        generateSimpleErrorMessage(notificationContext.getErrors()))
                )
                .build();
        }
    }

    private void linkedCaseNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();
        if (cicCase.getSubjectCIC() != null && !cicCase.getSubjectCIC().isEmpty()) {
            caseLinkedNotification.sendToSubject(data, caseNumber);
        }
        if (cicCase.getApplicantCIC() != null && !cicCase.getApplicantCIC().isEmpty()) {
            caseLinkedNotification.sendToApplicant(data, caseNumber);
        }
        if (cicCase.getRepresentativeCIC() != null && !cicCase.getRepresentativeCIC().isEmpty()) {
            caseLinkedNotification.sendToRepresentative(data, caseNumber);
        }
    }
}
