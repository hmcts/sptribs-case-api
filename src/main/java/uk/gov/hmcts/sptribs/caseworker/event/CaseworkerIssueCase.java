package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseIssuedNotification;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessage;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@RequiredArgsConstructor
@Component
@Slf4j
public class CaseworkerIssueCase implements CCDConfig<CaseData, State, UserRole> {

    @Value("${case-api.url}")
    private String baseUrl;

    private static final CcdPageConfiguration issueCaseNotifyParties = new IssueCaseNotifyParties();
    private static final CcdPageConfiguration issueCaseSelectDocument = new IssueCaseSelectDocument();

    private final CaseIssuedNotification caseIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ISSUE_CASE)
                .forStates(CaseManagement)
                .name("Case: Issue to respondent")
                .description("Case: Issue to respondent")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_JUDGE)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        issueCaseSelectDocument.addTo(pageBuilder);
        issueCaseNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        DynamicMultiSelectList documentList = DocumentListUtil.prepareDocumentList(caseData, baseUrl);
        caseData.getCaseIssue().setDocumentList(documentList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final String caseNumber = data.getHyphenatedCaseRef();
        final List<String> errors = new ArrayList<>();

        if (!isEmpty(cicCase.getNotifyPartySubject())) {
            try {
                caseIssuedNotification.sendToSubject(details.getData(), caseNumber);
            } catch (Exception notificationException) {
                errors.add(SUBJECT.getLabel());
            }
        }
        if (!isEmpty(cicCase.getNotifyPartyApplicant())) {
            try {
                caseIssuedNotification.sendToApplicant(details.getData(), caseNumber);
            } catch (Exception notificationException) {
                errors.add(APPLICANT.getLabel());
            }
        }
        if (!isEmpty(cicCase.getNotifyPartyRepresentative())) {
            try {
                caseIssuedNotification.sendToRepresentative(details.getData(), caseNumber);
            } catch (Exception notificationException) {
                errors.add(REPRESENTATIVE.getLabel());
            }
        }
        if (!isEmpty(cicCase.getNotifyPartyRespondent())) {
            try {
                caseIssuedNotification.sendToRespondent(details.getData(), caseNumber);
            } catch (Exception notificationException) {
                errors.add(RESPONDENT.getLabel());
            }
        }

        if (isEmpty(errors)) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case issued %n##  This case has now been issued. %n## %s",
                    generateSimpleMessage(details.getData().getCicCase())))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Issue case notification failed %n## %s %n## Please resend the notification.",
                        generateSimpleErrorMessage(errors))
                )
                .build();
        }
    }
}
