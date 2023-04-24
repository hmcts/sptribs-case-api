package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class AdjournedReasonsPage implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("adjournmentReason","outcome = \"Adjourned\"");
        pageBuilder.page("adjournmentReason")
            .pageShowConditions(map)
            .pageLabel("Adjournment reasons")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getAdjournmentReasons)
            .done();
    }
}
