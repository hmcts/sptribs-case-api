package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.APPELLANT_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_FLAG;


public class ManageFlagShowList implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseFlagDynamicList=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerManageFlagSelect", this::midEvent)
            .pageLabel("Select case flag")
            .label("LabelCaseworkerManageFlagSelect", "")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getAppellantFlags,ALWAYS_HIDE)
            .readonly(CicCase::getCaseFlags,ALWAYS_HIDE)
            .readonly(CicCase::getRespondentFlags,ALWAYS_HIDE)
            .mandatory(CicCase::getFlagDynamicList)
            .label("error", "<h2>There are no flags on case to manage</h2>",
                "cicCaseCaseFlags =\"\" AND cicCaseAppellantFlags =\"\" AND cicCaseRespondentFlags =\"\"")
            .done();

    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final List<String> errors = new ArrayList<>();
        String selectedFlag = data.getCicCase().getFlagDynamicList().getValue().getLabel();
        String[] selectedList = selectedFlag.split(HYPHEN);
        if (selectedList[0].equals(CASE_FLAG)) {
            for (ListValue<Flags> listValueFlag : cicCase.getCaseFlags()) {
                if (Objects.equals(listValueFlag.getId(), selectedList[1])) {
                    cicCase.setSelectedFlag(listValueFlag.getValue());
                    break;
                }
            }
        } else if (selectedList[0].equals(APPELLANT_FLAG)) {
            for (ListValue<Flags> listValueFlag : cicCase.getAppellantFlags()) {
                if (Objects.equals(listValueFlag.getId(), selectedList[1])) {
                    cicCase.setSelectedFlag(listValueFlag.getValue());
                    break;
                }
            }
        } else if (selectedList[0].equals(RESPONDENT_FLAG)) {
            for (ListValue<Flags> listValueFlag : cicCase.getRespondentFlags()) {
                if (Objects.equals(listValueFlag.getId(), selectedList[1])) {
                    cicCase.setSelectedFlag(listValueFlag.getValue());
                    break;
                }
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
