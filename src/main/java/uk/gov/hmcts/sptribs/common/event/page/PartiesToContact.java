package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

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
            .optionalWithoutDefaultValue(CicCase::getNotifyPartySubject,
                "cicCaseFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getApplicantFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyApplicant,
                "cicCaseApplicantFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRespondentName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRespondent,
                "cicCaseRespondentName!=\"\" ", RECIPIENT_LABEL)
            .mandatory(CicCase::getNotifyPartyMessage)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final List<String> errors = new ArrayList<>();


        if (null != data.getContactParties() && CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())
            && CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {

            errors.add("Which parties do you want to contact is required.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
