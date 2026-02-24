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
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectBundle;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.EDIT_BUNDLE;
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
public class CaseworkerEditBundle implements CCDConfig<CaseData, State, UserRole> {


    private static final SelectBundle selectBundle = new SelectBundle();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(EDIT_BUNDLE)
            .forStates(CaseManagement, AwaitingHearing)
            .name("Bundle: Amend bundle")
            .description("Bundle: Amend bundle")
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

        selectBundle.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        var cicCase = caseData.getCicCase();

        List<DynamicListElement> dynamicListElements = new ArrayList<>();

        for (ListValue<Bundle> bundle : caseData.getCaseBundles())  {
            DynamicListElement dynamicListElement = new DynamicListElement();
            dynamicListElement.setCode(UUID.randomUUID());
            dynamicListElement.setLabel(bundle.getValue().getDateAndTime() + " -- " + bundle.getValue().getFileName());

            dynamicListElements.add(dynamicListElement);
        }

        DynamicMultiSelectList bundleList = DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .build();

        cicCase.setAmendBundleList(bundleList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        final CaseData caseData = details.getData();
        CicCase cicCase = caseData.getCicCase();

        DynamicMultiSelectList bundleList = cicCase.getAmendBundleList();
        List<DynamicListElement> selectedBundleLabels = bundleList.getValue();

        // if there were no bundles to amend...
        if (selectedBundleLabels.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        // get the timestamps of the bundles to be deleted from the labels of the selected bundles
        List<String> timestampsOfBundlesToDelete = new ArrayList<>();
        for (DynamicListElement selectedBundleLabel : selectedBundleLabels) {
            String bundleTimestamp = selectedBundleLabel.getLabel().split(DOUBLE_HYPHEN)[0].trim();
            timestampsOfBundlesToDelete.add(bundleTimestamp);
        }

        // search the bundles by their timestamp for the ones to delete
        List<ListValue<Bundle>> allBundles = caseData.getCaseBundles();
        List<ListValue<Bundle>> selectedBundlesToDelete = new ArrayList<>();
        for (ListValue<Bundle> bundleListValue : allBundles) {
            if (timestampsOfBundlesToDelete.contains(bundleListValue.getValue().getDateAndTime().toString())) {
                selectedBundlesToDelete.add(bundleListValue);
            }
        }

        List<String> idsOfBundlesToBeRemoved = new ArrayList<>();
        List<ListValue<BundleIdAndTimestamp>> idsAndTimestampsOfBundlesToBeRemoved = new ArrayList<>();

        // delete selected bundles and save their ids so that they can be removed from the list of bundle ids and timestamps
        for (ListValue<Bundle> bundle : selectedBundlesToDelete) {
            idsOfBundlesToBeRemoved.add(bundle.getValue().getId());
            caseData.getCaseBundles().remove(bundle);
        }

        // search, by their ids, the ids and timestamps of the bundles to be removed
        for (ListValue<BundleIdAndTimestamp> bundleIdAndTimestamp : caseData.getCaseBundleIdsAndTimestamps()) {
            if (idsOfBundlesToBeRemoved.contains(bundleIdAndTimestamp.getValue().getBundleId())) {
                idsAndTimestampsOfBundlesToBeRemoved.add(bundleIdAndTimestamp);
            }
        }

        // delete the ids and timestamps of the bundles to be removed
        for (ListValue<BundleIdAndTimestamp> bundleIdAndTimestampToBeRemoved : idsAndTimestampsOfBundlesToBeRemoved) {
            caseData.getCaseBundleIdsAndTimestamps().remove(bundleIdAndTimestampToBeRemoved);
        }

        fixListValueIds(caseData.getCaseBundles(), caseData.getCaseBundleIdsAndTimestamps());

        caseData.getCicCase().setAmendBundleList(new DynamicMultiSelectList());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    // make sure list value IDs are sequential starting from 1 after deletion of bundles so that there are no collection ID dupes
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
