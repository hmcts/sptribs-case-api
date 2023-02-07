package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.PartiesToContact;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerContactParties implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration partiesToContact = new PartiesToContact();


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_CONTACT_PARTIES)
                .forStates(Draft,
                    Withdrawn,
                    Rejected,
                    Submitted,
                    NewCaseReceived,
                    CaseManagement,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Case: Contact parties")
                .showSummary()
                .aboutToSubmitCallback(this::abutToSubmit)
                .submittedCallback(this::partiesContacted)
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));
        partiesToContact.addTo(pageBuilder);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> abutToSubmit(CaseDetails<CaseData, State> details,
                                                                      CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse partiesContacted(CaseDetails<CaseData, State> details,
                                                      CaseDetails<CaseData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Message sent %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getContactParties())
            ))
            .build();

    }

}
