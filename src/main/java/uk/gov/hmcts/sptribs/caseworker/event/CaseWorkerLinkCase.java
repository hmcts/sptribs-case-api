package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.LinkCaseSelectCase;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.links.LinkService;
import uk.gov.hmcts.sptribs.common.notification.CaseLinkedNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_LINK_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
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
public class CaseWorkerLinkCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration linkCaseSelectCase = new LinkCaseSelectCase();
    @Autowired
    LinkService linkService;

    @Autowired
    CaseLinkedNotification caseLinkedNotification;
    @Value("${feature.link-case.enabled}")
    private boolean linkCaseEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (linkCaseEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_LINK_CASE)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome)
            .name("Links: Link case")
            .showSummary()
            .description("Links: Link case")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE));

        addWarning(pageBuilder);
        linkCaseSelectCase.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();

        caseData.getCicCase().setLinkCaseReason(linkService.getLinkReasons());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker link the case callback invoked for Case Id: {}", details.getId());

        var data = details.getData();
        var caseNumber = (null != data.getCicCase().getLinkCaseNumber()
            && null != data.getCicCase().getLinkCaseNumber().getNumber())
            ? data.getCicCase().getLinkCaseNumber().getNumber().replace("-", "") : "";
        CaseLink caseLink = CaseLink.builder()
            .caseReference(caseNumber)
            .createdDateTime(null)
            .caseType("CriminalInjuriesCompensation")
            .build();

        //TODO  .reasonForLink(Set.of(data.getCicCase().getLinkCaseReason().getLabel()))
        if (CollectionUtils.isEmpty(data.getCaseLinks())) {
            List<ListValue<CaseLink>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<CaseLink>builder()
                .id("1")
                .value(caseLink)
                .build();

            listValues.add(listValue);

            data.setCaseLinks(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<CaseLink>builder()
                .value(caseLink)
                .build();

            data.getCaseLinks().add(0, listValue); // always add new note as first element so that it is displayed on top

            data.getCaseLinks().forEach(
                caseLinkListValue -> caseLinkListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        String claimNumber = data.getHyphenatedCaseRef();
        try {
            sendCaseStayedNotification(claimNumber, data);
        } catch (Exception notificationException) {
            log.error("Case Link notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case Link notification failed %n## Please resend the notification"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(MessageUtil.generateSimpleMessage(
                "Link added to case", ""))
            .build();
    }

    private void addWarning(PageBuilder pageBuilder) {
        pageBuilder.page("beforeYouStart")
            .pageLabel("Before you start")
            .label("beforeYouStartLabel",
                """
                    If a group of linked cases has a lead case, you must start from the lead case.

                    If the cases to be linked has no lead, you can start the linking journey from any of those cases
                    """);
    }

    private void sendCaseStayedNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (!cicCase.getSubjectCIC().isEmpty()) {
            caseLinkedNotification.sendToSubject(data, caseNumber);
        }

        if (!cicCase.getApplicantCIC().isEmpty()) {
            caseLinkedNotification.sendToApplicant(data, caseNumber);
        }

        if (!cicCase.getRepresentativeCIC().isEmpty()) {
            caseLinkedNotification.sendToRepresentative(data, caseNumber);
        }
    }


}
