package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.AudioVideoEvidenceBundleException;
import uk.gov.hmcts.sptribs.document.bundling.AudioVideoEvidenceBundleService;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingService;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleDocument;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleFolder;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleIdAndTimestamp;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;
import uk.gov.hmcts.sptribs.document.model.AbstractCaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.dispatcher.BundleCreatedNotification;
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.extractDocumentsFromListValues;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocuments;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateCombinedNotificationMessageFromSentAndErrorLists;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessageFromSentList;
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
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.AUDIO_VIDEO_EVIDENCE_FOLDER_NAME;

@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class CaseworkerCreateBundle implements CCDConfig<CaseData, State, UserRole> {

    private final BundlingService bundlingService;
    private final NotificationDispatcher notificationDispatcher;
    private final AudioVideoEvidenceBundleService audioVideoEvidenceBundleService;
    private final Clock clock;
    private final BundleCreatedNotification bundleCreatedNotification;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

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
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE)
                .publishToCamunda();

        if (citizenDashboardEnabled) {
            eventBuilder.submittedCallback(this::submitted);
        }

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

        try {
            caseData.setAudioVideoEvidenceBundleDocument(
                audioVideoEvidenceBundleService
                    .createAudioVideoEvidenceBundleDocument(details.getId())
                    .orElse(null)
            );
        } catch (AudioVideoEvidenceBundleException exception) {
            log.error("Unable to create audio/video evidence document for case {}", details.getId(), exception);
            clearTemporaryBundleData(caseData);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(
                    "The audio/video evidence document could not be created. "
                        + "No bundle has been created. Please try again."
                ))
                .build();
        }

        details.setData(caseData);

        final Callback callback = new Callback(details, beforeDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        List<ListValue<Bundle>> existingBundles = getExistingBundles(beforeDetails);
        try {
            caseData.setCaseBundles(getConfiguredCaseBundles(caseData, bundleCallback, existingBundles));
        } catch (RuntimeException exception) {
            deleteAudioVideoEvidenceBundleDocument(caseData);
            throw exception;
        }

        if (caseData.getCaseBundles() == null) {
            deleteAudioVideoEvidenceBundleDocument(caseData);
        }

        clearTemporaryBundleData(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final String caseReference = caseData.getHyphenatedCaseRef();

        if (details.getState() == CaseClosed) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader("# Bundle created.")
                .build();
        }

        NotificationContext notificationContext = NotificationContext.forNotification(
            caseData,
            caseReference,
            bundleCreatedNotification);

        notificationDispatcher.sendToCorrespondenceParties(notificationContext);

        if (isEmpty(notificationContext.getErrors()) && !isEmpty(notificationContext.getSent())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Bundle created. %n## %s",
                    generateSimpleMessageFromSentList(notificationContext.getSent())))
                .build();
        }
        if (!isEmpty(notificationContext.getSent()) && !isEmpty(notificationContext.getErrors())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(generateCombinedNotificationMessageFromSentAndErrorLists(
                    notificationContext.getSent(), notificationContext.getErrors(), "Bundle created."))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Bundle creation notification failed %n## %s %n## Please resend the notification.",
                        generateSimpleErrorMessage(notificationContext.getErrors())))
                .build();
        }
    }

    private void setCaseBundleRequestDocuments(CaseData caseData, List<CaseworkerCICDocument> allDocuments) {
        List<CaseworkerCICDocument> initialDocuments = extractDocumentsFromListValues(caseData.getInitialCicaDocuments());

        if (!CollectionUtils.isEmpty(initialDocuments)) {
            caseData.setCaseDocuments(convertToBundleDocumentType(initialDocuments));
            caseData.setFurtherCaseDocuments(convertToBundleDocumentTypeFurtherDocs(getFurtherDocuments(allDocuments, initialDocuments)));
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

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> convertToBundleDocumentTypeFurtherDocs(
        List<CaseworkerCICDocument> docs) {
        return docs.stream()
            .filter(CaseworkerCICDocument::isValidBundleDocument)
            .map(this::updateFurtherDocumentFileNames)
            .map(AbstractCaseworkerCICDocument::new)
            .toList();
    }

    private CaseworkerCICDocument updateFurtherDocumentFileNames(CaseworkerCICDocument doc) {
        String filename = doc.getDocumentLink().getFilename();
        String category = doc.getDocumentCategory() != null ? doc.getDocumentCategory().getType() : null;
        String updatedFilename = filename != null && category != null ? category + " - " + filename : filename;

        return CaseworkerCICDocument.builder()
            .documentCategory(doc.getDocumentCategory())
            .documentEmailContent(doc.getDocumentEmailContent())
            .documentLink(Document.builder()
                .url(doc.getDocumentLink().getUrl())
                .binaryUrl(doc.getDocumentLink().getBinaryUrl())
                .categoryId(doc.getDocumentCategory().getCategory())
                .filename(updatedFilename)
                .build())
            .date(doc.getDate())
            .build();
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
        List<ListValue<Bundle>> caseBundles = bundlingService.buildBundleListValues(bundlingService.createBundle(bundleCallback,
            Long.parseLong(caseData.getCaseNumber())));

        if (caseBundles == null) {
            return null;
        }

        List<ListValue<BundleDocument>> audioVideoDocuments = caseData.getAudioVideoEvidenceBundleDocument() != null
            ? audioVideoEvidenceBundleService.createAudioVideoBundleDocuments(Long.parseLong(caseData.getCaseNumber()))
            : Collections.emptyList();

        configureAudioVideoEvidenceFolder(caseBundles, audioVideoDocuments, existingBundles);

        applyBundleTimestamps(caseData, caseBundles, existingBundles);

        return caseBundles;
    }

    private void applyBundleTimestamps(CaseData caseData,
                                       List<ListValue<Bundle>> caseBundles,
                                       List<ListValue<Bundle>> existingBundles) {
        List<ListValue<BundleIdAndTimestamp>> bundleIdsAndTimestamps =
            Optional.ofNullable(caseData.getCaseBundleIdsAndTimestamps()).orElse(new ArrayList<>());

        Map<String, LocalDateTime> bundleIdToTimestampMap = buildBundleIdToTimestampMap(bundleIdsAndTimestamps);

        Set<String> existingBundleIds = existingBundles.stream()
            .map(bundle -> bundle.getValue().getId())
            .collect(Collectors.toCollection(HashSet::new));

        for (ListValue<Bundle> caseBundle : caseBundles) {
            updateBundleTimestamp(caseBundle.getValue(), bundleIdToTimestampMap, existingBundleIds, bundleIdsAndTimestamps);
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
    }

    private Map<String, LocalDateTime> buildBundleIdToTimestampMap(List<ListValue<BundleIdAndTimestamp>> bundleIdsAndTimestamps) {
        Map<String, LocalDateTime> bundleIdToTimestampMap = new HashMap<>();
        for (ListValue<BundleIdAndTimestamp> item : bundleIdsAndTimestamps) {
            if (item != null && item.getValue() != null) {
                String bundleId = item.getValue().getBundleId();
                if (bundleId != null) {
                    bundleIdToTimestampMap.putIfAbsent(bundleId, item.getValue().getDateAndTime());
                }
            }
        }
        return bundleIdToTimestampMap;
    }

    private void updateBundleTimestamp(Bundle bundle,
                                       Map<String, LocalDateTime> bundleIdToTimestampMap,
                                       Set<String> existingBundleIds,
                                       List<ListValue<BundleIdAndTimestamp>> bundleIdsAndTimestamps) {
        String bundleId = bundle.getId();

        if (bundleIdToTimestampMap.containsKey(bundleId)) {
            // Case 1: Bundle has a known timestamp in our stored data - restore it
            bundle.setDateAndTime(bundleIdToTimestampMap.get(bundleId));
        } else if (existingBundleIds.contains(bundleId)) {
            // Case 2: Old bundle that existed before the timestamp workaround was implemented
            // Leave timestamp as null for backwards compatibility
            bundle.setDateAndTime(null);
        } else {
            // Case 3: Truly new bundle - set current timestamp and record it
            LocalDateTime now = LocalDateTime.now(clock);
            bundle.setDateAndTime(now);

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

    private void configureAudioVideoEvidenceFolder(List<ListValue<Bundle>> caseBundles,
                                                   List<ListValue<BundleDocument>> audioVideoDocuments,
                                                   List<ListValue<Bundle>> existingBundles) {
        final Map<String, List<ListValue<BundleFolder>>> existingFoldersMap = buildExistingFoldersMap(existingBundles);

        for (ListValue<Bundle> caseBundle : caseBundles) {
            if (caseBundle != null && caseBundle.getValue() != null) {
                configureBundleAudioVideoEvidenceFolder(caseBundle.getValue(), audioVideoDocuments, existingFoldersMap);
            }
        }
    }

    private Map<String, List<ListValue<BundleFolder>>> buildExistingFoldersMap(List<ListValue<Bundle>> existingBundles) {
        final Map<String, List<ListValue<BundleFolder>>> existingFoldersMap = new HashMap<>();
        for (ListValue<Bundle> existingBundle : existingBundles) {
            if (existingBundle != null && existingBundle.getValue() != null && existingBundle.getValue().getId() != null) {
                existingFoldersMap.put(existingBundle.getValue().getId(), existingBundle.getValue().getFolders());
            }
        }
        return existingFoldersMap;
    }

    /**
     * Configures the audio/video evidence folder for a given bundle.
     *
     * <p>Why this is needed:
     * When a bundle is generated, the bundling service returns all bundles for the case, including pre-existing
     * historical bundles. Because audio/video evidence documents are dynamically evaluated for the case at the
     * time this callback runs:
     * <ul>
     *   <li>Pre-existing bundles must retain their original folders and documents to prevent retroactive mutations
     *       (e.g., stripping historical media folders or polluting them with newly uploaded media).</li>
     *   <li>Newly created bundles must reflect current evidence: if audio/video evidence exists, the designated
     *       folder is created or updated; if none exists, any placeholder folder from the template is removed
     *       to avoid empty folders in the bundle index.</li>
     * </ul>
     *
     * @param bundle the bundle to configure
     * @param audioVideoDocuments current audio/video evidence documents for the case
     * @param existingFoldersMap map of bundle IDs to original folder lists for pre-existing bundles
     */
    private void configureBundleAudioVideoEvidenceFolder(Bundle bundle,
                                                         List<ListValue<BundleDocument>> audioVideoDocuments,
                                                         Map<String, List<ListValue<BundleFolder>>> existingFoldersMap) {
        String bundleId = bundle.getId();

        if (bundleId != null && existingFoldersMap.containsKey(bundleId)) {
            bundle.setFolders(existingFoldersMap.get(bundleId));
            return;
        }

        if (CollectionUtils.isEmpty(audioVideoDocuments)) {
            removeAudioVideoEvidenceFolder(bundle);
        } else {
            addOrUpdateAudioVideoEvidenceFolder(bundle, audioVideoDocuments);
        }
    }

    private void removeAudioVideoEvidenceFolder(Bundle bundle) {
        List<ListValue<BundleFolder>> folders = bundle.getFolders();
        if (folders == null) {
            return;
        }

        List<ListValue<BundleFolder>> updatedFolders = folders.stream()
            .filter(folder -> folder != null
                && folder.getValue() != null
                && !AUDIO_VIDEO_EVIDENCE_FOLDER_NAME.equalsIgnoreCase(folder.getValue().getName()))
            .collect(Collectors.toCollection(ArrayList::new));

        bundle.setFolders(updatedFolders.isEmpty() ? null : updatedFolders);
    }

    private void addOrUpdateAudioVideoEvidenceFolder(Bundle bundle,
                                                     List<ListValue<BundleDocument>> audioVideoDocuments) {
        List<ListValue<BundleFolder>> folders = bundle.getFolders();
        if (folders == null) {
            folders = new ArrayList<>();
            bundle.setFolders(folders);
        }

        Optional<ListValue<BundleFolder>> audioVideoFolder = folders.stream()
            .filter(folder -> folder != null
                && folder.getValue() != null
                && AUDIO_VIDEO_EVIDENCE_FOLDER_NAME.equalsIgnoreCase(folder.getValue().getName()))
            .findFirst();

        if (audioVideoFolder.isPresent()) {
            audioVideoFolder.get().getValue().setDocuments(audioVideoDocuments);
        } else {
            folders.add(createAudioVideoEvidenceFolder(folders, audioVideoDocuments));
        }
    }

    private ListValue<BundleFolder> createAudioVideoEvidenceFolder(List<ListValue<BundleFolder>> folders,
                                                                   List<ListValue<BundleDocument>> audioVideoDocuments) {
        int sortIndex = folders.stream()
            .filter(Objects::nonNull)
            .map(ListValue::getValue)
            .filter(Objects::nonNull)
            .mapToInt(BundleFolder::getSortIndex)
            .max()
            .orElse(0) + 1;

        BundleFolder newFolder = BundleFolder.builder()
            .name(AUDIO_VIDEO_EVIDENCE_FOLDER_NAME)
            .sortIndex(sortIndex)
            .documents(audioVideoDocuments)
            .build();

        return ListValue.<BundleFolder>builder()
            .id(String.valueOf(folders.size() + 1))
            .value(newFolder)
            .build();
    }

    private void clearTemporaryBundleData(CaseData caseData) {
        caseData.setMultiBundleConfiguration(null);
        caseData.setCaseDocuments(null);
        caseData.setFurtherCaseDocuments(null);
        caseData.setAudioVideoEvidenceBundleDocument(null);
    }

    private void deleteAudioVideoEvidenceBundleDocument(CaseData caseData) {
        if (caseData.getAudioVideoEvidenceBundleDocument() != null) {
            audioVideoEvidenceBundleService.deleteAudioVideoEvidenceBundleDocument(
                caseData.getAudioVideoEvidenceBundleDocument().getDocumentLink()
            );
        }
    }
}
