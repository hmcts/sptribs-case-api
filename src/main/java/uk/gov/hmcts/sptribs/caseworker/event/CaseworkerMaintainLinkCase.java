package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.sptribs.common.notification.CaseUnlinkedNotification;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MAINTAIN_LINK_CASE;
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
public class CaseworkerMaintainLinkCase implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALWAYS_HIDE = "LinkedCasesComponentLauncher = \"DONOTSHOW\"";

    @Value("${feature.link-case.enabled}")
    private boolean linkCaseEnabled;

    private final CaseUnlinkedNotification caseUnlinkedNotification;
    
    @Autowired
    public CaseworkerMaintainLinkCase(CaseUnlinkedNotification caseUnlinkedNotification) {
        this.caseUnlinkedNotification = caseUnlinkedNotification;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (linkCaseEnabled) {
            new PageBuilder(configBuilder
                .event(CASEWORKER_MAINTAIN_LINK_CASE)
                .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome)
                .name("Manage case links")
                .submittedCallback(this::linkUpdated)
                .description("To maintain linked cases")
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
                    ST_CIC_JUDGE))
                .page("maintainCaseLink")
                .pageLabel("Maintain Case Link")
                .optional(CaseData::getCaseLinks, ALWAYS_HIDE, null, true)
                .optional(CaseData::getLinkedCasesComponentLauncher,
                    null, null, null, null, "#ARGUMENT(UPDATE,LinkedCases)");
        }
    }

    public SubmittedCallbackResponse linkUpdated(CaseDetails<CaseData, State> details,
                                                 CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        try {
            unLinkedCaseNotification(data.getHyphenatedCaseRef(), data);
        } catch (Exception notificationException) {
            log.error("Case Link notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case Link updated %n"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Link updated %n"))
            .build();
    }


    private void unLinkedCaseNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (null != cicCase.getSubjectCIC() && !cicCase.getSubjectCIC().isEmpty()) {
            caseUnlinkedNotification.sendToSubject(data, caseNumber);
        }

        if (null != cicCase.getApplicantCIC() && !cicCase.getApplicantCIC().isEmpty()) {
            caseUnlinkedNotification.sendToApplicant(data, caseNumber);
        }

        if (null != cicCase.getRepresentativeCIC() && !cicCase.getRepresentativeCIC().isEmpty()) {
            caseUnlinkedNotification.sendToRepresentative(data, caseNumber);
        }
    }

}
