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
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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
            return getBundleFromResponse((List<LinkedHashMap<String, Object>>) response.getData().get("caseBundles"));
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
        for (int i = 0; i < response.size(); i++) {
            LinkedHashMap<String, Object> objectLinkedHashMap = (LinkedHashMap<String, Object>) response.get(i).get("value");
            Bundle bundle = Bundle.builder()
                .stitchStatus("New")
                .description(null != objectLinkedHashMap.get("description") ? objectLinkedHashMap.get("description").toString() : "")
                .id(null != objectLinkedHashMap.get("id") ? objectLinkedHashMap.get("id").toString() : "")
                .title(null != objectLinkedHashMap.get("title") ? objectLinkedHashMap.get("title").toString() : "")
                .stitchedDocument(null != objectLinkedHashMap.get("stitchedDocument")
                    ? (Document) objectLinkedHashMap.get("stitchedDocument") : null)
                .paginationStyle(null != objectLinkedHashMap.get("paginationStyle")
                    ? BundlePaginationStyle.valueOf(objectLinkedHashMap.get("paginationStyle").toString()) : null)
                .pageNumberFormat(null != objectLinkedHashMap.get("pageNumberFormat")
                    ? PageNumberFormat.valueOf(objectLinkedHashMap.get("pageNumberFormat").toString()) : null)
                .stitchingFailureMessage(null != objectLinkedHashMap.get("stitchingFailureMessage")
                    ? objectLinkedHashMap.get("stitchingFailureMessage").toString() : "")
                .stitchStatus(null != objectLinkedHashMap.get("stitchingStatus")
                    ? objectLinkedHashMap.get("stitchingStatus").toString() : "")
                .build();

            if (null != objectLinkedHashMap.get("folders")) {
                List<LinkedHashMap<String, Object>> responseFolders
                    = (List<LinkedHashMap<String, Object>>) objectLinkedHashMap.get("folders");
                for (int y = 0; y < response.size(); y++) {
                    LinkedHashMap<String, Object> foldersObject = (LinkedHashMap<String, Object>) responseFolders.get(y).get("value");
                    BundleFolder bundleFolder = BundleFolder.builder()
                        .name(null != foldersObject.get("name") ? foldersObject.get("name").toString() : "")
                        .sortIndex(null != foldersObject.get("sortIndex") ? (Integer) foldersObject.get("sortIndex") : null)
                        .build();
                    if (null != foldersObject.get("documents")) {
                        bundleFolder.setDocuments(buildBundleDocumentListValues(getDocuments(foldersObject)));
                    }
                    folders.add(bundleFolder);
                }

            }
            if (null != objectLinkedHashMap.get("documents")) {
                bundle.setDocuments(buildBundleDocumentListValues(getDocuments(objectLinkedHashMap)));
            }
            bundle.setFolders(buildBundleFolderListValues(folders));
            bundleList.add(bundle);
        }

        return bundleList;
    }

    public List<ListValue<Bundle>> buildBundleListValues(List<Bundle> bundleList) {
        List<ListValue<Bundle>> newList = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }
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
        List<ListValue<BundleFolder>> newList = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }
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
        List<ListValue<BundleDocument>> newList = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        if (CollectionUtils.isEmpty(bundleList)) {
            return null;
        }
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

    private List<BundleDocument> getDocuments(LinkedHashMap<String, Object> response) {
        List<BundleDocument> documents = new ArrayList<>();
        if (null != response.get("documents")) {
            List<LinkedHashMap<String, Object>> documentsFromResponse = (List<LinkedHashMap<String, Object>>) response.get("documents");
            for (int z = 0; z < documentsFromResponse.size(); z++) {
                LinkedHashMap<String, Object> document = (LinkedHashMap<String, Object>) documentsFromResponse.get(z).get("value");
                BundleDocument bundleDocument = BundleDocument.builder()
                    .name(null != document.get("name") ? document.get("name").toString() : "")
                    .name(null != document.get("description") ? document.get("description").toString() : "")
                    .sortIndex(null != document.get("sortIndex") ? (Integer) document.get("sortIndex") : null)
                    .build();
                documents.add(bundleDocument);
            }
            return documents;
        }
        return null;
    }
}
