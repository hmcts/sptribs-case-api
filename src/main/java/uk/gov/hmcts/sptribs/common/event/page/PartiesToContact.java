package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PartiesToContact implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("partiesToContact", this::midEvent)
            .label("contactPartiesLabel", "Which parties do you want to contact?")
            .complex(CaseData::getContactParties)
            .optional(ContactParties::getSubjectContactParties)
            .optional(ContactParties::getRepresentativeContactParties, "cicCaseRepresentativeFullName!=\"\" ")
            .optional(ContactParties::getRespondant)
            .mandatory(ContactParties::getMessage)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

//        if (null != data.getContactParties() && null == data.getContactParties().getRepresentativeContactParties()
//            && null ==  data.getContactParties().getSubjectContactParties()
//            && null ==  data.getContactParties().getRespondant()) {
//
//            errors.add("Which parties do you want to contact?. is required.");
//
//        }


        if (null != data.getContactParties() && data.getContactParties().getRepresentativeContactParties().size() == 0
            && data.getContactParties().getSubjectContactParties().size() == 0
            && data.getContactParties().getRespondant().size() == 0) {

            errors.add("Which parties do you want to contact?. is required.");

        }


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}

