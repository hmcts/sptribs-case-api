package uk.gov.hmcts.sptribs.document.bundling.client;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleDocument;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleFolder;
import uk.gov.hmcts.sptribs.document.bundling.model.BundlePaginationStyle;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;
import uk.gov.hmcts.sptribs.document.bundling.model.MultiBundleConfig;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.CASE_BUNDLES;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DESCRIPTION;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENTS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENT_URL;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.FOLDERS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.ID;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.NAME;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.NEW;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.PAGE_NUMBER_FORMAT;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.PAGINATION_STYLE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.SORT_INDEX;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.STITCHED_DOCUMENT;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.STITCHING_FAILURE_MESSAGE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.STITCHING_STATUS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.TITLE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.VALUE;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class BundlingService {

    private static final String BUNDLE_FILE_NAME = "st_cic_bundle_all_case.yaml";

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private BundlingClient bundlingClient;

    @Autowired
    private Clock clock;

    public List<Bundle> createBundle(Callback callback) {
        BundleResponse response;
        try {
            response = bundlingClient.createBundle(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                callback);

            return getBundleFromResponse((List<LinkedHashMap<String, Object>>) response.getData().get(CASE_BUNDLES));
        } catch (FeignException exception) {
            log.error("Unable to create bundle {}",
                exception.getMessage());
            return null;
        }
    }

    public MultiBundleConfig getMultiBundleConfig() {
        return MultiBundleConfig.builder().value(BUNDLE_FILE_NAME).build();
    }

    public List<MultiBundleConfig> getMultiBundleConfigs() {
        return List.of(MultiBundleConfig.builder().value(BUNDLE_FILE_NAME).build());
    }

    public List<ListValue<Bundle>> buildBundleListValues(List<Bundle> bundleList) {
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }

        AtomicInteger listValueIndex = new AtomicInteger(0);
        List<ListValue<Bundle>> newList = new ArrayList<>();
        for (Bundle doc : bundleList) {
            ListValue<Bundle> listValue = ListValue
                .<Bundle>builder()
                .value(doc)
                .build();

            newList.add(0, listValue);
            newList.forEach(
                document -> document.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return newList;
    }

    public List<ListValue<BundleFolder>> buildBundleFolderListValues(List<BundleFolder> bundleList) {
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }

        List<ListValue<BundleFolder>> newList = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        for (BundleFolder doc : bundleList) {
            ListValue<BundleFolder> listValue = ListValue
                .<BundleFolder>builder()
                .value(doc)
                .build();

            newList.add(0, listValue);
            newList.forEach(
                document -> document.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return newList;
    }

    public List<ListValue<BundleDocument>> buildBundleDocumentListValues(List<BundleDocument> bundleList) {
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }

        List<ListValue<BundleDocument>> newList = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        for (BundleDocument doc : bundleList) {
            final ListValue<BundleDocument> listValue = ListValue
                .<BundleDocument>builder()
                .value(doc)
                .build();

            newList.add(0, listValue);
            newList.forEach(
                document -> document.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return newList;
    }

    private List<Bundle> getBundleFromResponse(List<LinkedHashMap<String, Object>> response) {
        List<Bundle> bundleList = new ArrayList<>();
        Optional.ofNullable(response).ifPresent(list ->
            list.forEach(res -> {
                LinkedHashMap<String, Object> objectLinkedHashMap = (LinkedHashMap<String, Object>) res.get(VALUE);
                Bundle bundle = buildBundle(objectLinkedHashMap);

                bundle.setFolders(buildBundleFolderListValues(buildBundleFolders(objectLinkedHashMap)));
                if (objectLinkedHashMap.get(DOCUMENTS) != null) {
                    List<Map<String, Object>> documentsFromResponse = (List<Map<String, Object>>) objectLinkedHashMap.get(DOCUMENTS);
                    bundle.setDocuments(buildBundleDocumentListValues(getDocuments(documentsFromResponse)));
                }
                bundleList.add(bundle);
            }));

        return bundleList;
    }

    private List<BundleFolder> buildBundleFolders(LinkedHashMap<String, Object> objectLinkedHashMap) {
        List<BundleFolder> folders = new ArrayList<>();
        if (objectLinkedHashMap.get(FOLDERS) != null) {
            List<LinkedHashMap<String, Object>> responseFolders
                = (List<LinkedHashMap<String, Object>>) objectLinkedHashMap.get(FOLDERS);
            for (LinkedHashMap<String, Object> responseFolder : responseFolders) {
                LinkedHashMap<String, Object> foldersObject = (LinkedHashMap<String, Object>) responseFolder.get(VALUE);
                BundleFolder bundleFolder = BundleFolder.builder()
                        .name(MapUtils.getString(foldersObject, NAME, ""))
                        .sortIndex(MapUtils.getIntValue(foldersObject, SORT_INDEX))
                        .build();

                if (foldersObject.get(DOCUMENTS) != null) {
                    List<Map<String, Object>> folderDocumentsList = (List<Map<String, Object>>) foldersObject.get(DOCUMENTS);
                    bundleFolder.setDocuments(buildBundleDocumentListValues(getDocuments(folderDocumentsList)));
                }
                folders.add(bundleFolder);
            }

        }
        return folders;
    }

    private Bundle buildBundle(LinkedHashMap<String, Object> objectLinkedHashMap) {
        return Bundle.builder()
            .stitchStatus(NEW)
            .description(MapUtils.getString(objectLinkedHashMap, DESCRIPTION, ""))
            .id(MapUtils.getString(objectLinkedHashMap, ID, ""))
            .dateAndTime(LocalDateTime.now(clock))
            .title(MapUtils.getString(objectLinkedHashMap, TITLE, ""))
            .stitchedDocument(getStitchedDocument(objectLinkedHashMap))
            .paginationStyle(BundlePaginationStyle.valueOf(
                MapUtils.getObject(objectLinkedHashMap, PAGINATION_STYLE, BundlePaginationStyle.off).toString()))
            .pageNumberFormat(PageNumberFormat.valueOf(
                MapUtils.getObject(objectLinkedHashMap, PAGE_NUMBER_FORMAT, PageNumberFormat.numberOfPages).toString()))
            .stitchingFailureMessage(MapUtils.getString(objectLinkedHashMap, STITCHING_FAILURE_MESSAGE, ""))
            .stitchStatus(MapUtils.getString(objectLinkedHashMap, STITCHING_STATUS, ""))
            .build();
    }

    private Document getStitchedDocument(LinkedHashMap<String, Object> objectLinkedHashMap) {
        if (ObjectUtils.isEmpty(objectLinkedHashMap.get(STITCHED_DOCUMENT))) {
            return null;
        }

        LinkedHashMap<String, Object> stitchedDocMap = (LinkedHashMap<String, Object>) objectLinkedHashMap.get(STITCHED_DOCUMENT);

        return Document.builder()
            .url(MapUtils.getString(stitchedDocMap, DOCUMENT_URL, ""))
            .binaryUrl(MapUtils.getString(stitchedDocMap, DOCUMENT_BINARY_URL, ""))
            .filename(MapUtils.getString(stitchedDocMap, DOCUMENT_FILENAME, ""))
            .build();
    }

    private List<BundleDocument> getDocuments(List<Map<String, Object>> documentsList) {
        List<BundleDocument> documents = new ArrayList<>();
        documentsList.forEach(listItem -> {
            Map<String, Object> document = (Map<String, Object>) listItem.get(VALUE);
            BundleDocument bundleDocument = BundleDocument.builder()
                .name(MapUtils.getString(document, NAME, ""))
                .description(MapUtils.getString(document, DESCRIPTION, ""))
                .sortIndex(MapUtils.getIntValue(document, SORT_INDEX))
                .build();
            documents.add(bundleDocument);
        });

        return documents;
    }
}
