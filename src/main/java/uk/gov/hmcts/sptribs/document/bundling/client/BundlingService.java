package uk.gov.hmcts.sptribs.document.bundling.client;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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


    public List<Bundle> createBundle(Callback callback) {
        BundleResponse response = null;
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
                if (null != objectLinkedHashMap.get(DOCUMENTS)) {
                    bundle.setDocuments(buildBundleDocumentListValues(getDocuments(objectLinkedHashMap)));
                }
                bundleList.add(bundle);
            }));

        return bundleList;
    }

    private List<BundleFolder> buildBundleFolders(LinkedHashMap<String, Object> objectLinkedHashMap) {
        List<BundleFolder> folders = new ArrayList<>();
        if (null != objectLinkedHashMap.get(FOLDERS)) {
            List<LinkedHashMap<String, Object>> responseFolders
                = (List<LinkedHashMap<String, Object>>) objectLinkedHashMap.get(FOLDERS);
            for (LinkedHashMap<String, Object> responseFolder : responseFolders) {
                LinkedHashMap<String, Object> foldersObject = (LinkedHashMap<String, Object>) responseFolder.get(VALUE);
                BundleFolder bundleFolder = BundleFolder.builder()
                        .name(null != foldersObject.get(NAME) ? foldersObject.get(NAME).toString() : "")
                        .sortIndex(null != foldersObject.get(SORT_INDEX) ? (Integer) foldersObject.get(SORT_INDEX) : null)
                        .build();
                if (null != foldersObject.get(DOCUMENTS)) {
                    bundleFolder.setDocuments(buildBundleDocumentListValues(getDocuments(foldersObject)));
                }
                folders.add(bundleFolder);
            }

        }
        return folders;
    }

    private Bundle buildBundle(LinkedHashMap<String, Object> objectLinkedHashMap) {
        return Bundle.builder()
            .stitchStatus(NEW)
            .description(null != objectLinkedHashMap.get(DESCRIPTION) ? objectLinkedHashMap.get(DESCRIPTION).toString() : "")
            .id(null != objectLinkedHashMap.get(ID) ? objectLinkedHashMap.get(ID).toString() : "")
            .title(null != objectLinkedHashMap.get(TITLE) ? objectLinkedHashMap.get(TITLE).toString() : "")
            .stitchedDocument(getStitchedDocument(objectLinkedHashMap))
            .paginationStyle(null != objectLinkedHashMap.get(PAGINATION_STYLE)
                ? BundlePaginationStyle.valueOf(objectLinkedHashMap.get(PAGINATION_STYLE).toString()) : null)
            .pageNumberFormat(null != objectLinkedHashMap.get(PAGE_NUMBER_FORMAT)
                ? PageNumberFormat.valueOf(objectLinkedHashMap.get(PAGE_NUMBER_FORMAT).toString()) : null)
            .stitchingFailureMessage(null != objectLinkedHashMap.get(STITCHING_FAILURE_MESSAGE)
                ? objectLinkedHashMap.get(STITCHING_FAILURE_MESSAGE).toString() : "")
            .stitchStatus(null != objectLinkedHashMap.get(STITCHING_STATUS)
                ? objectLinkedHashMap.get(STITCHING_STATUS).toString() : "")
            .build();
    }

    private Document getStitchedDocument(LinkedHashMap<String, Object> objectLinkedHashMap) {
        if (ObjectUtils.isEmpty(objectLinkedHashMap.get(STITCHED_DOCUMENT))) {
            return null;
        }

        LinkedHashMap stitchedDocMap = (LinkedHashMap) objectLinkedHashMap.get(STITCHED_DOCUMENT);
        Document stitchedDocument = Document.builder()
            .url(null != stitchedDocMap && null != stitchedDocMap.get(DOCUMENT_URL)
                ? stitchedDocMap.get(DOCUMENT_URL).toString() : "")
            .binaryUrl(null != stitchedDocMap && null != stitchedDocMap.get(DOCUMENT_BINARY_URL)
                ? stitchedDocMap.get(DOCUMENT_BINARY_URL).toString() : "")
            .filename(null != stitchedDocMap && null != stitchedDocMap.get(DOCUMENT_FILENAME)
                ? stitchedDocMap.get(DOCUMENT_FILENAME).toString() : "")
            .build();

        return stitchedDocument;
    }

    private List<BundleDocument> getDocuments(Map<String, Object> response) {
        List<BundleDocument> documents = new ArrayList<>();
        if (null != response.get(DOCUMENTS)) {
            List<Map<String, Object>> documentsFromResponse = (List<Map<String, Object>>) response.get(DOCUMENTS);
            documentsFromResponse.forEach(res -> {
                Map<String, Object> document = (Map<String, Object>) res.get(VALUE);
                BundleDocument bundleDocument = BundleDocument.builder()
                    .name(null != document.get(NAME) ? document.get(NAME).toString() : "")
                    .description(null != document.get(DESCRIPTION) ? document.get(DESCRIPTION).toString() : "")
                    .sortIndex(null != document.get(SORT_INDEX) ? (Integer) document.get(SORT_INDEX) : null)
                    .build();
                documents.add(bundleDocument);
            });

            return documents;
        }
        return null;
    }
}
