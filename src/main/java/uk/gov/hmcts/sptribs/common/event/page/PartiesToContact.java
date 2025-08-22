package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_CONTACT_PARTY;

@Slf4j
@Component
public class PartiesToContact implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "[STATE]=\"ALWAYS_HIDE\"";
    private static final String RECIPIENT_LABEL = "Contact parties recipient";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("partiesToContact", this::midEvent)
            .pageLabel("Contact Parties")
            .label("LabelPartiesToContactMessage", "Which parties do you want to contact?")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getFullName,
                "cicCaseFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getApplicantFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getApplicantFullName,
                "cicCaseApplicantFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getRepresentativeFullName,
                "cicCaseRepresentativeFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRespondentName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getRespondentName,
                "cicCaseRespondentName!=\"\" ", RECIPIENT_LABEL)
            .mandatory(CicCase::getNotifyPartyMessage)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final List<String> errors = new ArrayList<>();

        if (cicCase != null) {
            if (isEmpty(cicCase.getNotifyPartySubject())
                && isEmpty(cicCase.getNotifyPartyRepresentative())
                && isEmpty(cicCase.getNotifyPartyApplicant())
                && isEmpty(cicCase.getNotifyPartyRespondent())) {
                errors.add(SELECT_AT_LEAST_ONE_CONTACT_PARTY);
            } else if ((cicCase.getCaseSubcategory() == CaseSubcategory.FATAL
                || cicCase.getCaseSubcategory() == CaseSubcategory.MINOR)
                && !isEmpty(cicCase.getNotifyPartySubject())) {
                errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
