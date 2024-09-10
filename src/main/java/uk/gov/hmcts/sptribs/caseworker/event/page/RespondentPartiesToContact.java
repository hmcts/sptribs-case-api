package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_CONTACT_PARTY;

@Slf4j
@Component
public class RespondentPartiesToContact implements CcdPageConfiguration {

    private static final String RECIPIENT_LABEL = "Contact parties recipient";
    private static final String ALWAYS_HIDE = "[STATE]=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("partiesToContact", this::midEvent)
            .pageLabel("Contact Parties")
            .label("LabelPartiesToContact", "Which parties do you want to contact?")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .readonly(CicCase::getApplicantFullName, ALWAYS_HIDE)
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getContactParties)
            .optionalWithoutDefaultValue(ContactParties::getSubjectContactParties,
                "cicCaseFullName!=\"\" ", RECIPIENT_LABEL)
            .optionalWithoutDefaultValue(ContactParties::getApplicantContactParties,
                "cicCaseApplicantFullName!=\"\" ", RECIPIENT_LABEL)
            .optionalWithoutDefaultValue(ContactParties::getRepresentativeContactParties,
                "cicCaseRepresentativeFullName!=\"\" ", RECIPIENT_LABEL)
            .optionalWithoutDefaultValue(ContactParties::getTribunal,
                null, RECIPIENT_LABEL)
            .done()
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getNotifyPartyMessage)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final ContactParties contactParties = data.getContactParties();
        final List<String> errors = new ArrayList<>();

        if (contactParties != null) {
            if (CollectionUtils.isEmpty(contactParties.getRepresentativeContactParties())
                && CollectionUtils.isEmpty(contactParties.getSubjectContactParties())
                && CollectionUtils.isEmpty(contactParties.getApplicantContactParties())
                && CollectionUtils.isEmpty(contactParties.getTribunal())) {
                errors.add(SELECT_AT_LEAST_ONE_CONTACT_PARTY);
            } else if ((cicCase.getCaseSubcategory() == CaseSubcategory.FATAL
                || cicCase.getCaseSubcategory() == CaseSubcategory.MINOR)
                && !CollectionUtils.isEmpty(contactParties.getSubjectContactParties())) {
                errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
