package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

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
                    .mandatory(CicCase::getAnonymiseYesOrNo)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDetails,
                                                                  CaseDetails<CaseData, State> caseDetailsBefore) {
        final CaseData caseData = caseDetails.getData();

        if (caseData.getCicCase().getAnonymiseYesOrNo().equals(YesOrNo.YES)
            && caseData.getCicCase().getAnonymisedAppellantName() == null ) {
            String anonymisedName = anonymisationService.getOrCreateAnonymisation();
            caseData.getCicCase().setAnonymisedAppellantName(anonymisedName);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}


