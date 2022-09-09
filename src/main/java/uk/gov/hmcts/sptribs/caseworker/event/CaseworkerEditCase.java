package uk.gov.hmcts.sptribs.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.caseworker.event.page.CaseCategorisationDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.ApplicantDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;

import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerEditCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_EDIT_CASE = "edit-case";
    private final CcdPageConfiguration editCaseCategorisationDetails = new CaseCategorisationDetails();
    private final CcdPageConfiguration editSelectedPartiesDetails = new SelectParties();
    private final CcdPageConfiguration editApplicantDetails = new ApplicantDetails();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = addEventConfig(configBuilder);
        editCaseCategorisationDetails.addTo(pageBuilder);
        editSelectedPartiesDetails.addTo(pageBuilder);
        editApplicantDetails.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_EDIT_CASE)
            .forStates(POST_SUBMISSION_STATES)
            .name("Edit Case")
            .description("")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER));
    }
}
