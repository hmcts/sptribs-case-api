package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SelectParties implements CcdPageConfiguration {



    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("objectSubjects", this::midEvent)
            .pageLabel("Who are the parties in this case?")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getPartiesCIC, "cicCaseCaseSubcategory!= \"Fatal\"")
            .optional(CicCase::getPartiesCICWithOutSubject,"cicCaseCaseSubcategory= \"Fatal\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

//        if (null != data.getCicCase() && !data.getCicCase().getPartiesCIC().contains(PartiesCIC.SUBJECT)) {
//            errors.add("Subject is mandatory.");
//        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
