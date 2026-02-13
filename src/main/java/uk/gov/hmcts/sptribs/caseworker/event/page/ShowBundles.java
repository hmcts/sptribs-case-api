package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ShowBundles implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showBundles", this::midEvent)
            .pageLabel("Show bundles")
            .label("LabelShowBundles", "")
            .list(CaseData::getCaseBundles)
            .readonly(Bundle::getId)
            .readonly(Bundle::getDateAndTime)
            .readonly(Bundle::getDocuments)
            .readonly(Bundle::getFileName)
            .list(Bundle::getFolders)
            .readonly(BundleFolder::getName)
            .readonly(BundleFolder::getDocuments)
            .readonly(BundleFolder::getFolders)
            .readonly(BundleFolder::getSortIndex)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CaseData oldData = detailsBefore.getData();
//        if (ObjectUtils.isEmpty(data.getRemovedCaseBundlesList())) {
//            List<ListValue<Bundle>> removedBundlesList = new ArrayList<>();
//            data.setRemovedCaseBundlesList(removedBundlesList);
//        }
        final List<String> errors = new ArrayList<>();

        // if there are bundles in the old data and there are no more bundles in the new data, or there are less bundles in the new data than the old data. . .
        if (!CollectionUtils.isEmpty(oldData.getCaseBundles())
            && (CollectionUtils.isEmpty(data.getCaseBundles())
            || data.getCaseBundles().size() < oldData.getCaseBundles().size())) {

            List<String> caseBundleIds = new ArrayList<>();
            for (ListValue<Bundle> bundle : data.getCaseBundles()) {
                caseBundleIds.add(bundle.getValue().getId());
            }

            for (ListValue<Bundle> bundle : oldData.getCaseBundles()) {
                if (!caseBundleIds.contains(bundle.getValue().getId())) {
                    addToRemovedBundles(data, bundle.getValue());
                }
            }
        }

        if (CollectionUtils.isEmpty(data.getRemovedCaseBundlesList())) {
            errors.add("Please remove at least one document to continue");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    public static void addToRemovedBundles(CaseData caseData, Bundle bundle) {
        if (CollectionUtils.isEmpty(caseData.getRemovedCaseBundlesList())) {
            List<ListValue<Bundle>> listValues = new ArrayList<>();

            ListValue<Bundle> listValue = ListValue
                .<Bundle>builder()
                .id("1")
                .value(bundle)
                .build();

            listValues.add(listValue);

            caseData.setRemovedCaseBundlesList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<Bundle> listValue = ListValue
                .<Bundle>builder()
                .value(bundle)
                .build();

            caseData.getRemovedCaseBundlesList().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getRemovedCaseBundlesList().forEach(
                removedBundleListValue -> removedBundleListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
    }
}
