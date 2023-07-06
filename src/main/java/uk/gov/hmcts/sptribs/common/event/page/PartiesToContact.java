package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;

@Slf4j
@Component
public class PartiesToContact implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "contactPartiesDocumentsDocumentList= \"ALWAYS_HIDE\"";
    private static final String RECIPIENT_LABEL = "Contact parties recipient";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("partiesToContact", this::midEvent)
            .pageLabel("Contact Parties")
            .label("LabelPartiesToContactMessage", "Which parties do you want to contact?")
            .complex(CaseData::getCicCase)
            .optionalWithLabel(CicCase::getNotifyPartySubject, RECIPIENT_LABEL)
            .readonly(CicCase::getApplicantFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyApplicant,
                "cicCaseApplicantFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ", RECIPIENT_LABEL)
            .optionalWithLabel(CicCase::getNotifyPartyRespondent, RECIPIENT_LABEL)

            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();


        if (null != data.getContactParties() && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRespondent())) {

            errors.add(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
        } else if ((data.getCicCase().getCaseSubcategory() == CaseSubcategory.FATAL
            || data.getCicCase().getCaseSubcategory() == CaseSubcategory.MINOR)
            && !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())) {
            errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}

