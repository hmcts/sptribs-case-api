package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CorrectPaperCase;
import uk.gov.hmcts.sptribs.caseworker.event.page.CorrectRelationshipDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.NewPaperCase;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Slf4j
@Component
public class CaseworkerCorrectPaperCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String CORRECT_PAPER_CASE = "caseworker-correct-paper-case";
    private final CcdPageConfiguration correctRelationshipDetails = new CorrectRelationshipDetails();
    private final CcdPageConfiguration correctPaperCase = new CorrectPaperCase();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = addEventConfig(configBuilder);
        correctRelationshipDetails.addTo(pageBuilder);
        correctPaperCase.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CORRECT_PAPER_CASE)
            .forState(NewPaperCase)
            .name("Correct paper case")
            .description("Correct paper case")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CORRECT_PAPER_CASE, details.getId());
        CaseData caseData = details.getData();

        caseData.getLabelContent().setApplicationType(caseData.getApplicationType());
        caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CORRECT_PAPER_CASE, details.getId());
        CaseData caseData = details.getData();
        caseData.getBulkScanMetaInfo().setWarnings(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
