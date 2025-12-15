package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.CreateAndSendIssuingType;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_TO_ANONYMISE_CASE;

@RequiredArgsConstructor
@Component
public class ApplyAnonymity implements CcdPageConfiguration {

    private final AnonymisationService anonymisationService;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerApplyAnonymity", this::midEvent)
                .pageLabel("Anonymity")
                .label("LabelCaseworkerApplyAnonymity", "")
                .complex(CaseData::getCicCase)
                    .readonly(CicCase::getFullName, "LabelCaseworkerApplyAnonymity!=\"\"")
                    .readonly(CicCase::getAnonymisedAppellantName, "LabelCaseworkerApplyAnonymity!=\"\"")
                    .readonly(CicCase::getAnonymityAlreadyApplied, "LabelCaseworkerApplyAnonymity!=\"\"")
                    .mandatory(CicCase::getAnonymiseYesOrNo)
                    .done()
                .readonly(CaseData::getCurrentEvent, "LabelCaseworkerApplyAnonymity=\"HIDDEN\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDetails,
                                                                  CaseDetails<CaseData, State> caseDetailsBefore) {
        final CaseData caseData = caseDetails.getData();
        final CicCase cicCase = caseData.getCicCase();
        final List<String> errors = new ArrayList<>();

        if (YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo()) && cicCase.getAnonymisedAppellantName() == null) {
            String anonymisedName = anonymisationService.getOrCreateAnonymisation();
            if (anonymisedName == null) {
                errors.add(FAILED_TO_ANONYMISE_CASE);
            }
            cicCase.setAnonymisedAppellantName(anonymisedName);
        }

        if (YesOrNo.NO.equals(cicCase.getAnonymityAlreadyApplied()) && YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo())) {
            cicCase.setCreateAndSendIssuingTypes(CreateAndSendIssuingType.CREATE_AND_SEND_NEW_ORDER);

            DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                    .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS)
                    .build();
            caseData.setDraftOrderContentCIC(draftOrderContentCIC);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }
}


