package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LabelContent;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CorrectRelationshipDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("correctPaperCaseDetails", this::midEvent)
            .pageLabel("Correct paper case")
            .label("Label-CorrectYourApplication", "### Your application details")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicantOrApplicant1UC, NEVER_SHOW)
            .done()
            .mandatory(CaseData::getDivorceOrDissolution)
            .mandatory(CaseData::getApplicationType)
            .complex(CaseData::getApplication)
                .mandatoryWithLabel(Application::getScreenHasMarriageCert,
                    "Does ${labelContentTheApplicantOrApplicant1} have the ${labelContentMarriageOrCivilPartnership} certificate?")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        final CaseData data = details.getData();

        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
