package uk.gov.hmcts.sptribs.document.bundling.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.CASE_BUNDLES;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DESCRIPTION;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENTS;
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
            log.info("response {}", response);
            return getBundleFromResponse((List<LinkedHashMap<String, Object>>) response.getData().get(CASE_BUNDLES));
        } catch (FeignException exception) {
            log.error("Unable to create bundle {}",
                exception.getMessage());
            return null;
        }
    }

    public List<MultiBundleConfig> getMultiBundleConfig() {
        return List.of(MultiBundleConfig.builder().value(BUNDLE_FILE_NAME).build());
    }

    private List<Bundle> getBundleFromResponse(List<LinkedHashMap<String, Object>> response) {
        List<Bundle> bundleList = new ArrayList<>();
        List<BundleFolder> folders = new ArrayList<>();

        response.forEach(res -> {
            LinkedHashMap<String, Object> objectLinkedHashMap = (LinkedHashMap<String, Object>) res.get(VALUE);
            Bundle bundle = Bundle.builder()
                .stitchStatus(NEW)
                .description(null != objectLinkedHashMap.get(DESCRIPTION) ? objectLinkedHashMap.get(DESCRIPTION).toString() : "")
                .id(null != objectLinkedHashMap.get(ID) ? objectLinkedHashMap.get(ID).toString() : "")
                .title(null != objectLinkedHashMap.get(TITLE) ? objectLinkedHashMap.get(TITLE).toString() : "")
                .stitchedDocument(null != objectLinkedHashMap.get(STITCHED_DOCUMENT)
                    ? (Document) objectLinkedHashMap.get(STITCHED_DOCUMENT) : null)
                .paginationStyle(null != objectLinkedHashMap.get(PAGINATION_STYLE)
                    ? BundlePaginationStyle.valueOf(objectLinkedHashMap.get(PAGINATION_STYLE).toString()) : null)
                .pageNumberFormat(null != objectLinkedHashMap.get(PAGE_NUMBER_FORMAT)
                    ? PageNumberFormat.valueOf(objectLinkedHashMap.get(PAGE_NUMBER_FORMAT).toString()) : null)
                .stitchingFailureMessage(null != objectLinkedHashMap.get(STITCHING_FAILURE_MESSAGE)
                    ? objectLinkedHashMap.get(STITCHING_FAILURE_MESSAGE).toString() : "")
                .stitchStatus(null != objectLinkedHashMap.get(STITCHING_STATUS)
                    ? objectLinkedHashMap.get(STITCHING_STATUS).toString() : "")
                .build();

            if (null != objectLinkedHashMap.get(FOLDERS)) {
                List<LinkedHashMap<String, Object>> responseFolders
                    = (List<LinkedHashMap<String, Object>>) objectLinkedHashMap.get(FOLDERS);
                for (int y = 0; y < response.size(); y++) {
                    LinkedHashMap<String, Object> foldersObject = (LinkedHashMap<String, Object>) responseFolders.get(y).get(VALUE);
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
            if (null != objectLinkedHashMap.get(DOCUMENTS)) {
                bundle.setDocuments(buildBundleDocumentListValues(getDocuments(objectLinkedHashMap)));
            }
            bundle.setFolders(buildBundleFolderListValues(folders));
            bundleList.add(bundle);
        });

        return bundleList;
    }

    public List<ListValue<Bundle>> buildBundleListValues(List<Bundle> bundleList) {
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }

        AtomicInteger listValueIndex = new AtomicInteger(0);
        List<ListValue<Bundle>> newList = new ArrayList<>();
        for (Bundle doc : bundleList) {
            var listValue = ListValue
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
            var listValue = ListValue
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
            var listValue = ListValue
                .<BundleDocument>builder()
                .value(doc)
                .build();

            newList.add(0, listValue);
            newList.forEach(
                document -> document.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return newList;
    }

    private List<BundleDocument> getDocuments(Map<String, Object> response) {
        List<BundleDocument> documents = new ArrayList<>();
        if (null != response.get(DOCUMENTS)) {
            List<Map<String, Object>> documentsFromResponse = (List<Map<String, Object>>) response.get(DOCUMENTS);
            documentsFromResponse.forEach(res -> {
                Map<String, Object> document = (Map<String, Object>) res.get(VALUE);
                BundleDocument bundleDocument = BundleDocument.builder()
                    .name(null != document.get(NAME) ? document.get(NAME).toString() : "")
                    .name(null != document.get(DESCRIPTION) ? document.get(DESCRIPTION).toString() : "")
                    .sortIndex(null != document.get(SORT_INDEX) ? (Integer) document.get(SORT_INDEX) : null)
                    .build();
                documents.add(bundleDocument);
            });

            return documents;
        }
        return null;
    }
}
