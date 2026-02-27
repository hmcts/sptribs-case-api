package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectBundles;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleIdAndTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.REMOVE_BUNDLES;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerRemoveBundles implements CCDConfig<CaseData, State, UserRole> {


    private static final SelectBundles selectBundles = new SelectBundles();

    private static final String PLACEHOLDER_STITCHED_DOCUMENT_FILENAME = "-cicBundle.pdf";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(REMOVE_BUNDLES)
            .forStates(CaseManagement, AwaitingHearing)
            .showCondition("caseBundles!=\"[]\"")
            .name("Bundle: Remove bundles")
            .description("Bundle: Remove bundles")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE));

        selectBundles.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        var cicCase = caseData.getCicCase();

        List<DynamicListElement> dynamicListElements = new ArrayList<>();

        for (ListValue<Bundle> bundle : caseData.getCaseBundles())  {
            DynamicListElement dynamicListElement = new DynamicListElement();
            dynamicListElement.setCode(UUID.randomUUID());
            dynamicListElement.setLabel(!(bundle.getValue().getStitchedDocument() == null)
                ? bundle.getValue().getDateAndTime() + " -- " + bundle.getValue().getStitchedDocument().getFilename()
                : bundle.getValue().getDateAndTime() + " -- " + PLACEHOLDER_STITCHED_DOCUMENT_FILENAME
            );

            dynamicListElements.add(dynamicListElement);
        }

        DynamicMultiSelectList bundleList = DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .build();

        cicCase.setRemoveBundlesList(bundleList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        final CaseData caseData = details.getData();

        List<DynamicListElement> selectedBundleLabels = caseData.getCicCase().getRemoveBundlesList().getValue();

        List<String> timestampsOfBundlesToDelete = new ArrayList<>();
        for (DynamicListElement selectedBundleLabel : selectedBundleLabels) {
            String bundleTimestamp = selectedBundleLabel.getLabel().split(DOUBLE_HYPHEN)[0].trim();
            timestampsOfBundlesToDelete.add(bundleTimestamp);
        }

        List<ListValue<Bundle>> allBundles = caseData.getCaseBundles();
        List<ListValue<Bundle>> selectedBundlesToDelete = new ArrayList<>();
        for (ListValue<Bundle> bundleListValue : allBundles) {
            if (timestampsOfBundlesToDelete.contains(bundleListValue.getValue().getDateAndTime().toString())) {
                selectedBundlesToDelete.add(bundleListValue);
            }
        }

        List<String> idsOfBundlesToBeRemoved = new ArrayList<>();
        List<ListValue<BundleIdAndTimestamp>> idsAndTimestampsOfBundlesToBeRemoved = new ArrayList<>();

        for (ListValue<Bundle> bundle : selectedBundlesToDelete) {
            idsOfBundlesToBeRemoved.add(bundle.getValue().getId());
            caseData.getCaseBundles().remove(bundle);
        }

        for (ListValue<BundleIdAndTimestamp> bundleIdAndTimestamp : caseData.getCaseBundleIdsAndTimestamps()) {
            if (idsOfBundlesToBeRemoved.contains(bundleIdAndTimestamp.getValue().getBundleId())) {
                idsAndTimestampsOfBundlesToBeRemoved.add(bundleIdAndTimestamp);
            }
        }

        for (ListValue<BundleIdAndTimestamp> bundleIdAndTimestampToBeRemoved : idsAndTimestampsOfBundlesToBeRemoved) {
            caseData.getCaseBundleIdsAndTimestamps().remove(bundleIdAndTimestampToBeRemoved);
        }

        fixListValueIds(caseData.getCaseBundles(), caseData.getCaseBundleIdsAndTimestamps());

        caseData.getCicCase().setRemoveBundlesList(new DynamicMultiSelectList());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public void fixListValueIds(List<ListValue<Bundle>> bundles, List<ListValue<BundleIdAndTimestamp>> bundleIdAndTimestamps) {
        final AtomicInteger listValueIndex = new AtomicInteger(0);
        for (ListValue<Bundle> bundle : bundles) {
            bundleIdAndTimestamps.get(listValueIndex.get()).setId(String.valueOf(listValueIndex.incrementAndGet()));
            bundle.setId(String.valueOf(listValueIndex));
        }
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }
}
