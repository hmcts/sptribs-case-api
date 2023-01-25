package uk.gov.hmcts.sptribs.document.content.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.FinancialOrderFor;

import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.sptribs.ciccase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.FinancialOrderFor.CHILDREN;

@Component
public class ApplicantTemplateDataProvider {

    private static final String FIN_ORDER_APPLICANT_CHILDREN_JOINT = "applicants, and for the children of both the applicants.";
    private static final String FIN_ORDER_CHILDREN_JOINT = "children of both the applicants.";
    private static final String FIN_ORDER_APPLICANTS = "applicants.";

    public String deriveJointFinancialOrder(Set<FinancialOrderFor> financialOrderFor) {

        if (!isEmpty(financialOrderFor)) {
            if (financialOrderFor.contains(APPLICANT) && financialOrderFor.contains(CHILDREN)) {
                return FIN_ORDER_APPLICANT_CHILDREN_JOINT;
            }

            if (financialOrderFor.contains(APPLICANT)) {
                return FIN_ORDER_APPLICANTS;
            }

            if (financialOrderFor.contains(CHILDREN)) {
                return FIN_ORDER_CHILDREN_JOINT;
            }
        }

        return null;
    }

}
