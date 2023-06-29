package uk.gov.hmcts.sptribs.common.event.page;

import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCICWithOutSubject;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SelectParties implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("objectSubjects", this::midEvent)
            .pageLabel("Who are the parties in this case?")
            .label("LabelObjectSubjects", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getPartiesCIC, "cicCaseCaseSubcategory!= \"Fatal\" AND cicCaseCaseSubcategory!= \"Minor\"")
            .optional(CicCase::getPartiesCICWithOutSubject, "cicCaseCaseSubcategory= \"Fatal\" OR cicCaseCaseSubcategory= \"Minor\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        Set<PartiesCIC> partiesCic = data.getCicCase().getPartiesCIC();
        Set<PartiesCICWithOutSubject> partiesCICWithoutSubject = data.getCicCase().getPartiesCICWithOutSubject();

        if (null != data.getCicCase() && null == partiesCic && partiesCICWithoutSubject.isEmpty()) {
            errors.add("One field is mandatory.");
        } else if (null != data.getCicCase() && null == partiesCICWithoutSubject && partiesCic.isEmpty()) {
            errors.add("One field is mandatory.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
