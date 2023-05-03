package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ManageFlagShowList;
import uk.gov.hmcts.sptribs.caseworker.event.page.ManageFlagUpdate;
import uk.gov.hmcts.sptribs.caseworker.service.FlagService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.Objects;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.APPELLANT_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MANAGE_CASE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_FLAG;
import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerManageCaseFlag implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    FlagService flagService;

    @Value("${feature.case-flags.enabled}")
    private boolean caseFlagsEnabled;

    private static final CcdPageConfiguration manageFlagShowList = new ManageFlagShowList();
    private static final CcdPageConfiguration manageFlagUpdate = new ManageFlagUpdate();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseFlagsEnabled) {
            doConfigure(configBuilder);
        }
    }

    public void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_MANAGE_CASE_FLAG)
            .forStates(POST_SUBMISSION_STATES)
            .name("Flags: Manage flags")
            .description("Flags: Manage flags")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER));
        manageFlagShowList.addTo(pageBuilder);
        manageFlagUpdate.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList flagList = flagService.populateFlagList(caseData.getCicCase());
        caseData.getCicCase().setFlagDynamicList(flagList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        var cicCase = caseData.getCicCase();
        String selectedFlag = caseData.getCicCase().getFlagDynamicList().getValue().getLabel();
        String[] selectedList = selectedFlag.split(HYPHEN);
        if (selectedList[0].equals(CASE_FLAG)) {
            for (ListValue<Flags> listValueFlag : cicCase.getCaseFlags()) {
                if (Objects.equals(listValueFlag.getId(), selectedList[1])) {
                    listValueFlag.getValue().getDetails().get(0).getValue().setFlagComment(cicCase.getFlagAdditionalDetail());
                    listValueFlag.getValue().getDetails().get(0).getValue().setStatus(cicCase.getFlagStatus().getLabel());
                    break;
                }
            }
        } else if (selectedList[0].equals(APPELLANT_FLAG)) {
            for (ListValue<Flags> listValueFlag : cicCase.getAppellantFlags()) {
                if (Objects.equals(listValueFlag.getId(), selectedList[1])) {
                    listValueFlag.getValue().getDetails().get(0).getValue().setFlagComment(cicCase.getFlagAdditionalDetail());
                    listValueFlag.getValue().getDetails().get(0).getValue().setStatus(cicCase.getFlagStatus().getLabel());
                    break;
                }
            }
        } else if (selectedList[0].equals(RESPONDENT_FLAG)) {
            for (ListValue<Flags> listValueFlag : cicCase.getRespondentFlags()) {
                if (Objects.equals(listValueFlag.getId(), selectedList[1])) {
                    listValueFlag.getValue().getDetails().get(0).getValue().setFlagComment(cicCase.getFlagAdditionalDetail());
                    listValueFlag.getValue().getDetails().get(0).getValue().setStatus(cicCase.getFlagStatus().getLabel());
                    break;
                }
            }
        }
        cicCase.setFlagAdditionalDetail("");
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .data(caseData)
            .build();

    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Flag updated")
            .build();
    }

}
