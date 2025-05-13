package uk.gov.hmcts.sptribs.common.event.page;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class SubjectDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("subjectDetailsObject", this::midEvent)
            .pageLabel("Who is the subject of this case?")
            .label("LabelSubject", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getFullName)
            .optional(CicCase::getPhoneNumber)
            .mandatoryWithLabel(CicCase::getDateOfBirth, "")
            .mandatory(CicCase::getAddress)
            .mandatoryWithLabel(CicCase::getContactPreferenceType, "")
            .mandatory(CicCase::getEmail, "cicCaseContactPreferenceType = \"Email\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCicCase() && null != data.getCicCase().getAddress()) {
            if (StringUtils.isEmpty(data.getCicCase().getAddress().getCountry())) {
                errors.add("Country is mandatory");
            }
            if (StringUtils.isEmpty(data.getCicCase().getAddress().getPostCode())) {
                errors.add("PostCode is mandatory");
            }
            if (StringUtils.isEmpty(data.getCicCase().getAddress().getPostTown())) {
                errors.add("Town or City is mandatory");
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
