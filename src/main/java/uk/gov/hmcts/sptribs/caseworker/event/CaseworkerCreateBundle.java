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
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingService;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.BUNDLE_STATES;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerCreateBundle implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    BundlingService bundlingService;

    @Value("${feature.bundling.enabled}")
    private boolean bundlingEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (bundlingEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CREATE_BUNDLE)
            .forStates(BUNDLE_STATES)
            .name("Bundle: Create a bundle")
            .description("Bundle: Create a bundle")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .grant(CREATE_READ_UPDATE, CIC_SUPER_USER,
                CIC_CASEWORKER, CIC_SENIOR_CASEWORKER, CIC_CENTRE_ADMIN,
                CIC_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                CIC_CASEWORKER,
                CIC_SENIOR_CASEWORKER,
                CIC_CENTRE_ADMIN,
                CIC_CENTRE_TEAM_LEADER,
                CIC_SENIOR_JUDGE,
                CIC_SUPER_USER,
                CIC_JUDGE))
            .page("createBundle")
            .pageLabel("Create a bundle")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker create bundle callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        caseData.setCaseDocuments(DocumentListUtil.getAllCaseDocuments(caseData));
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfig());
        Callback callback = new Callback(details, beforeDetails, CREATE_BUNDLE, true);
        BundleCallback bundleCallback = new BundleCallback(callback);

        caseData.setCaseBundles(bundlingService.buildBundleListValues(bundlingService.createBundle(bundleCallback)));

        caseData.setMultiBundleConfiguration(null);
        caseData.setCaseDocuments(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
