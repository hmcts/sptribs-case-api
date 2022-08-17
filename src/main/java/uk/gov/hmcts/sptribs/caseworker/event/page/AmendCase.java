package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LabelContent;
import uk.gov.hmcts.sptribs.ciccase.model.MarriageDetails;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class AmendCase implements CcdPageConfiguration {
    private static final String ALWAYS_HIDE = "marriageCountryOfMarriage=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("amendCase")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getTheApplicant2UC, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .optionalWithLabel(MarriageDetails::getDate,
                        "Date of ${labelContentMarriageOrCivilPartnership}")
                    .optionalWithLabel(MarriageDetails::getPlaceOfMarriage,
                        "Place of ${labelContentMarriageOrCivilPartnership}")
                    .optionalWithLabel(MarriageDetails::getCountryOfMarriage,
                        "Country of ${labelContentMarriageOrCivilPartnership}")
                    .optional(MarriageDetails::getApplicant1Name)
                    .optional(MarriageDetails::getApplicant2Name)
                .done()
            .done();
    }
}
