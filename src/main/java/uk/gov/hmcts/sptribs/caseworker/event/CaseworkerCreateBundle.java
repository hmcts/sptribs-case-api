package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
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
import uk.gov.hmcts.sptribs.notification.dispatcher.BundleCreatedNotification;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseIssuedNotification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.extractDocumentsFromListValues;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocuments;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessageBundleCreation;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
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

    private final CaseIssuedNotification caseIssuedNotification;

    @Autowired
    private final Clock clock;

    @Autowired
    private BundleCreatedNotification bundleCreatedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CREATE_BUNDLE)
                .forStates(CaseManagement, AwaitingHearing, ReadyToList, CaseClosed)
                .name("Bundle: Create a bundle")
                .description("Bundle: Create a bundle")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
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
        final List<CaseworkerCICDocument> allCaseDocuments = extractDocumentsFromListValues(getAllCaseDocuments(caseData));

        if (caseData.isBundleOrderEnabled()) {
            setCaseBundleRequestDocuments(caseData, allCaseDocuments);
        } else {
            var cicDocumentList = convertToBundleDocumentType(allCaseDocuments);
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

        List<ListValue<Bundle>> existingBundles = getExistingBundles(beforeDetails);
        caseData.setCaseBundles(getConfiguredCaseBundles(caseData, bundleCallback, existingBundles));

        caseData.setMultiBundleConfiguration(null);
        caseData.setCaseDocuments(null);
        caseData.setFurtherCaseDocuments(null);


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final String caseNumber = data.getHyphenatedCaseRef();
        final List<String> errors = new ArrayList<>();

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            try {
                bundleCreatedNotification.sendToRespondent(data, caseNumber);
            } catch (Exception notificationException) {
                errors.add(RESPONDENT.getLabel());
            }
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            try {
                bundleCreatedNotification.sendToRepresentative(data, caseNumber);
            } catch (Exception notificationException) {
                errors.add(REPRESENTATIVE.getLabel());
            }
        }
        if (CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && !CollectionUtils.isEmpty(cicCase.getApplicantCIC())) {
            try {
                bundleCreatedNotification.sendToApplicant(data, caseNumber);
            } catch (Exception notificationException) {
                errors.add(APPLICANT.getLabel());
            }
        }

        if (isEmpty(errors)) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Bundle created. %n## %s",
                    generateSimpleMessageBundleCreation(details.getData().getCicCase())))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Bundle creation notification failed %n## %s %n## Please resend the notification.",
                        generateSimpleErrorMessage(errors))
                )
                .build();
        }
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> getInitialCicaUpload(CaseData caseData, long caseId) {
        var initialDocs = Optional.ofNullable(caseData.getInitialCicaDocuments()).orElse(emptyList());
    private void setCaseBundleRequestDocuments(CaseData caseData, List<CaseworkerCICDocument> allDocuments) {
        List<CaseworkerCICDocument> initialDocuments = extractDocumentsFromListValues(caseData.getInitialCicaDocuments());

        if (!CollectionUtils.isEmpty(initialDocuments)) {
            caseData.setCaseDocuments(convertToBundleDocumentType(initialDocuments));
            caseData.setFurtherCaseDocuments(convertToBundleDocumentType(getFurtherDocuments(allDocuments, initialDocuments)));
        } else {
            caseData.setCaseDocuments(convertToBundleDocumentType(allDocuments));
        }
    }

    private static List<CaseworkerCICDocument> getFurtherDocuments(List<CaseworkerCICDocument> allDocuments,
                                                                   List<CaseworkerCICDocument> initialDocuments) {
        if (CollectionUtils.isEmpty(allDocuments)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(initialDocuments)) {
            return new ArrayList<>(allDocuments);
        }
        return allDocuments.stream().filter(doc -> !initialDocuments.contains(doc))
            .sorted(Comparator.comparing(
                CaseworkerCICDocument::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> convertToBundleDocumentType(List<CaseworkerCICDocument> docs) {

        return docs.stream().filter(CaseworkerCICDocument::isValidBundleDocument).map(AbstractCaseworkerCICDocument::new).toList();
    }

    private List<ListValue<Bundle>> getExistingBundles(CaseDetails<CaseData, State> beforeDetails) {
        if (beforeDetails == null || beforeDetails.getData() == null) {
            return emptyList();
        }
        return Optional.ofNullable(beforeDetails.getData().getCaseBundles()).orElse(emptyList());
    }

    private List<ListValue<Bundle>> getConfiguredCaseBundles(CaseData caseData,
                                                             BundleCallback bundleCallback,
                                                             List<ListValue<Bundle>> existingBundles) {
        List<ListValue<Bundle>> caseBundles = bundlingService.buildBundleListValues(bundlingService.createBundle(bundleCallback));

        if (caseBundles == null) {
            return null;
        }

        List<ListValue<BundleIdAndTimestamp>> bundleIdsAndTimestamps =
            Optional.ofNullable(caseData.getCaseBundleIdsAndTimestamps()).orElse(new ArrayList<>());

        Map<String, LocalDateTime> bundleIdToTimestampMap = new HashMap<>();
        for (ListValue<BundleIdAndTimestamp> item : bundleIdsAndTimestamps) {
            if (item != null && item.getValue() != null) {
                String bundleId = item.getValue().getBundleId();
                if (bundleId != null && !bundleIdToTimestampMap.containsKey(bundleId)) {
                    bundleIdToTimestampMap.put(bundleId, item.getValue().getDateAndTime());
                }
            }
        }

        Set<String> existingBundleIds = existingBundles.stream()
            .map(bundle -> bundle.getValue().getId())
            .collect(Collectors.toCollection(HashSet::new));

        for (ListValue<Bundle> caseBundle : caseBundles) {
            String bundleId = caseBundle.getValue().getId();

            if (bundleIdToTimestampMap.containsKey(bundleId)) {
                // Case 1: Bundle has a known timestamp in our stored data - restore it
                caseBundle.getValue().setDateAndTime(bundleIdToTimestampMap.get(bundleId));
            } else if (existingBundleIds.contains(bundleId)) {
                // Case 2: Old bundle that existed before the timestamp workaround was implemented
                // Leave timestamp as null for backwards compatibility
                caseBundle.getValue().setDateAndTime(null);
            } else {
                // Case 3: Truly new bundle - set current timestamp and record it
                LocalDateTime now = LocalDateTime.now(clock);
                caseBundle.getValue().setDateAndTime(now);

                String listValueId = String.valueOf(bundleIdsAndTimestamps.size() + 1);
                BundleIdAndTimestamp bundleIdAndTimestamp = BundleIdAndTimestamp.builder()
                    .bundleId(bundleId)
                    .dateAndTime(now)
                    .build();
                bundleIdsAndTimestamps.add(ListValue.<BundleIdAndTimestamp>builder()
                    .id(listValueId)
                    .value(bundleIdAndTimestamp)
                    .build());
            }
        }

        caseData.setCaseBundleIdsAndTimestamps(bundleIdsAndTimestamps);

        if (caseBundles.size() > 1) {
            caseBundles.sort(
                Comparator.comparing(
                    bundle -> bundle.getValue().getDateAndTime(),
                    Comparator.nullsLast(Comparator.reverseOrder())
                )
            );
        }

        return caseBundles;
    }
}
