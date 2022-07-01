package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.ciccase.model.Application;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.ciccase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class ConditionalOrderReviewApplicant1 implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("ConditionalOrderReviewApplicant1")
            .pageLabel("Review the applicant's application - Draft Conditional Order Application")
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
            .mandatory(ConditionalOrderQuestions::getConfirmInformationStillCorrect)
            .mandatory(ConditionalOrderQuestions::getReasonInformationNotCorrect,
                "coApplicant1ConfirmInformationStillCorrect=\"No\"")
                .done()
            .done();
    }
}
