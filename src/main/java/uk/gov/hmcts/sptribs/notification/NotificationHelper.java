package uk.gov.hmcts.sptribs.notification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;

@Component
public class NotificationHelper {

    public Map<String, Object> commonTemplateVars(final CicCase cicCase, final String caseNumber) {
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(TRIBUNAL_NAME, "Criminal Injuries Compensation Tribunal");
        templateVars.put(CIC_CASE_NUMBER, caseNumber);
        templateVars.put(CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        return templateVars;
    }
}
