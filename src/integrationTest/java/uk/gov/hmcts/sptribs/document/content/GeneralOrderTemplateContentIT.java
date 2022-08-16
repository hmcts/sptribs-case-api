package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralOrderJudgeOrLegalAdvisorType.ASSISTANT_JUSTICES_CLERK;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralOrderJudgeOrLegalAdvisorType.PROPER_OFFICER_OF_THE_COURT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.GENERAL_ORDER_MADE_BY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getGeneralOrder;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GeneralOrderTemplateContentIT {

    @Autowired
    private GeneralOrderTemplateContent generalOrderTemplateContent;

    @Test
    public void shouldApplyAssistantJusticeClerkContent() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(ASSISTANT_JUSTICES_CLERK);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(GENERAL_ORDER_MADE_BY, "an assistant justices clerk")
        );
    }

    @Test
    public void shouldApplyProperOfficerOfTheCourtContent() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getGeneralOrder().setGeneralOrderJudgeOrLegalAdvisorType(PROPER_OFFICER_OF_THE_COURT);
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");

        Map<String, Object> templateContent = generalOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(GENERAL_ORDER_MADE_BY, "a proper officer of the court")
        );
    }
}
