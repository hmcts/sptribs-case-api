package uk.gov.hmcts.sptribs.citizen.event;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

import static java.lang.String.format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicCreateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    AppsConfig appsConfig;

    @Autowired
    private DssApplicationReceivedNotification dssApplicationReceivedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                .getCreateEvent())
            .initialState(State.Draft)
            .name("Create draft case (DSS)")
            .description("Apply for edge case (DSS)")
            .grant(CREATE_READ_UPDATE, CITIZEN_CIC, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .retries(120, 120);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();
        caseData.setHyphenatedCaseRef(details.getData().formatCaseRef(details.getId()));
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.DSS_Draft)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        String caseNumber = data.getHyphenatedCaseRef();

        sendApplicationReceivedNotification(caseNumber, data);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Created %n## Case reference number: %n## %s", caseNumber))
            .build();
    }

    private void sendApplicationReceivedNotification(String caseNumber, CaseData data) {
        DssCaseData dssCaseData = data.getDssCaseData();

        if (!dssCaseData.getSubjectFullName().isEmpty()) {
            dssApplicationReceivedNotification.sendToSubject(data, caseNumber);
        }

        if (!dssCaseData.getRepresentativeFullName().isEmpty()) {
            dssApplicationReceivedNotification.sendToApplicant(data, caseNumber);
        }

        if (!dssCaseData.getRepresentativeFullName().isEmpty()) {
            dssApplicationReceivedNotification.sendToRepresentative(data, caseNumber);
        }
    }

    private void setIsRepresentativePresent(CaseData data) {
        if (null != data.getDssCaseData().getRepresentativeFullName()) {
            data.getDssCaseData().setIsRepresentativePresent(YesOrNo.YES);
        } else {
            data.getDssCaseData().setIsRepresentativePresent(YesOrNo.NO);
        }
    }


}
