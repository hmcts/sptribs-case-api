package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingService;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleIdAndTimestamp;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;
import uk.gov.hmcts.sptribs.document.model.AbstractCaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class CaseworkerCreateBundle implements CCDConfig<CaseData, State, UserRole> {

    private final BundlingService bundlingService;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CREATE_BUNDLE)
                .forStates(CaseManagement, AwaitingHearing, ReadyToList)
                .name("Bundle: Create a bundle")
                .description("Bundle: Create a bundle")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE)
                .publishToCamunda();

        new PageBuilder(eventBuilder)
                .page("createBundle")
                .pageLabel("Create a bundle")
                .done();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final List<ListValue<CaseworkerCICDocument>> documentListValues = DocumentListUtil.getAllCaseDocuments(caseData);

        if (caseData.isBundleOrderEnabled()) {
            caseData.setCaseDocuments(getInitialCicaUpload(caseData, details.getId()));
            caseData.setFurtherCaseDocuments(getFurtherDocuments(caseData));
        } else {
            var cicDocumentList = convertToBundleDocumentType(documentListValues);
            caseData.setCaseDocuments(cicDocumentList);
        }

        caseData.setBundleConfiguration(bundlingService.getMultiBundleConfig());
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());
        caseData.setCaseNumber(String.valueOf(details.getId()));
        caseData.setSubjectRepFullName(caseData.getCicCase().getFullName());
        caseData.setSchemeLabel(caseData.getCicCase().getSchemeCic() != null ? caseData.getCicCase().getSchemeCic().getLabel() : "");
        details.setData(caseData);

        final Callback callback = new Callback(details, beforeDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        caseData.setCaseBundles(getConfiguredCaseBundles(caseData, bundleCallback));

        caseData.setMultiBundleConfiguration(null);
        caseData.setCaseDocuments(null);
        caseData.setFurtherCaseDocuments(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> getInitialCicaUpload(CaseData caseData, long caseId) {
        var initialDocs = Optional.ofNullable(caseData.getInitialCicaDocuments()).orElse(emptyList());

        if (initialDocs.isEmpty()) {
            log.warn("Initial Cica doc upload was empty for case {}", caseId);
            return emptyList();
        }

        return convertToBundleDocumentType(initialDocs);
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> getFurtherDocuments(CaseData caseData) {
        var docs = getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        var bundleDocuments = convertToBundleDocumentType(docs);

        bundleDocuments.sort(
            Comparator.comparing(
                doc -> doc.getValue().getDate(),
                Comparator.nullsLast(Comparator.naturalOrder())
            )
        );
        return bundleDocuments;
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> convertToBundleDocumentType(
        List<ListValue<CaseworkerCICDocument>> docs) {
        List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> abstractCaseworkerCICDocumentList = new ArrayList<>();

        for (ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue : docs) {
            CaseworkerCICDocument document = caseworkerCICDocumentListValue.getValue();
            if (document.isValidBundleDocument()) {
                abstractCaseworkerCICDocumentList.add(new AbstractCaseworkerCICDocument<>(document));
            }
        }

        return abstractCaseworkerCICDocumentList;
    }

    private List<ListValue<Bundle>> getConfiguredCaseBundles(CaseData caseData, BundleCallback bundleCallback) {
        List<ListValue<Bundle>> caseBundles = bundlingService.buildBundleListValues(bundlingService.createBundle(bundleCallback));

        if (caseBundles == null) {
            return null;
        }

        ArrayList<String> bundleIds = new ArrayList<>();

        List<ListValue<BundleIdAndTimestamp>> bundleIdsAndTimestamps = caseData.getCaseBundleIdsAndTimestamps();

        Map<String, LocalDateTime> bundleIdToTimestampMap = new HashMap<>();

        for (ListValue<BundleIdAndTimestamp> caseBundleIDAndTimestamp : bundleIdsAndTimestamps) {
            bundleIds.add(caseBundleIDAndTimestamp.getValue().getBundleId());
            bundleIdToTimestampMap.put(
                caseBundleIDAndTimestamp.getValue().getBundleId(),
                caseBundleIDAndTimestamp.getValue().getDateAndTime()
            );
        }

        for (ListValue<Bundle> caseBundle : caseBundles) {
            String listValueID = "1";
            if (!bundleIdsAndTimestamps.isEmpty()) {
                listValueID = String.valueOf(bundleIdsAndTimestamps.size() + 1);
            }

            if (!bundleIds.contains(caseBundle.getValue().getId())) {
                BundleIdAndTimestamp bundleIdAndTimestamp = BundleIdAndTimestamp.builder()
                    .bundleId(caseBundle.getValue().getId())
                    .dateAndTime(LocalDateTime.now(clock))
                    .build();
                bundleIdsAndTimestamps.add(ListValue.<BundleIdAndTimestamp>builder()
                    .id(listValueID)
                    .value(bundleIdAndTimestamp)
                    .build());
                caseData.setCaseBundleIdsAndTimestamps(bundleIdsAndTimestamps);
                caseBundle.getValue().setDateAndTime(bundleIdAndTimestamp.getDateAndTime());
            } else {
                caseBundle.getValue().setDateAndTime(
                    bundleIdToTimestampMap.get(caseBundle.getValue().getId())
                );
            }
        }

        if (caseBundles.size() > 1) {
            caseBundles.sort((bundle1, bundle2) -> bundle2.getValue().getDateAndTime()
                .compareTo(bundle1.getValue().getDateAndTime()));
        }

        return caseBundles;
    }
}
