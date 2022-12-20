package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseStayedNotification;

import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerStayTheCase implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_STAY_THE_CASE = "caseworker-stay-the-case";

    @Autowired
    private CaseStayedNotification caseStayedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_STAY_THE_CASE)
            .forStates(CaseManagement, CaseStayed)
            .name("Stay the Case")
            .showSummary()
            .description("Add a Stay to this case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::stayed)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR))
            .page("addStay")
            .label("addStay", "<H2>Add a Stay to this case</H2>")
            .complex(CaseData::getCaseStay)
            .mandatoryWithLabel(CaseStay::getStayReason, "")
            .mandatory(CaseStay::getFlagType, "stayStayReason = \"Other\"")
            .mandatoryWithLabel(CaseStay::getExpirationDate, "")
            .optional(CaseStay::getAdditionalDetail, "");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker stay the case callback invoked for Case Id: {}", details.getId());
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseStayed)
            .build();
    }

    public SubmittedCallbackResponse stayed(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        String claimNumber = data.getHyphenatedCaseRef();

        sendCaseStayedNotification(claimNumber, data);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Stay Added to Case")
            .build();
    }

    private void sendCaseStayedNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (!cicCase.getSubjectCIC().isEmpty()) {
            caseStayedNotification.sendToSubject(data, caseNumber);
        }

        if (!cicCase.getApplicantCIC().isEmpty()) {
            caseStayedNotification.sendToApplicant(data, caseNumber);
        }

        if (!cicCase.getRepresentativeCIC().isEmpty()) {
            caseStayedNotification.sendToRepresentative(data, caseNumber);
        }
    }
}
