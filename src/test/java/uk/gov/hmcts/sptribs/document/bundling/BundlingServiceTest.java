package uk.gov.hmcts.sptribs.document.bundling;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.bundling.client.BundleResponse;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingClient;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingService;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleDocument;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleFolder;
import uk.gov.hmcts.sptribs.document.bundling.model.BundlePaginationStyle;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;
import uk.gov.hmcts.sptribs.document.bundling.model.MultiBundleConfig;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class BundlingServiceTest {

    public static final String BUNDLE_FILE_NAME = "st_cic_bundle_all_case.yaml";
    public static final String FOLDER_DOCUMENT_NAME = "folderDocument";
    public static final String DESCRIPTION = "some description";
    public static final String FOLDER_NAME = "folderName";
    public static final String DOCUMENT_NAME = "documentName";
    public static final String DOCUMENT_NAME_1 = "documentName1";
    public static final String DOCUMENT_NAME_2 = "documentName2";
    public static LinkedHashMap<String, Object> BUNDLE_LIST_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_LIST_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT;
    public static LinkedHashMap<String, Object> BUNDLE_LIST_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_LIST_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_LIST_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_LIST_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS;

    @InjectMocks
    private BundlingService bundlingService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private BundlingClient bundlingClient;

    private CaseData caseData;

    private CaseDetails<CaseData, State> updatedCaseDetails;

    private CaseDetails<CaseData, State> beforeCaseDetails;

    @BeforeEach
    void setUp() {
        caseData = caseData();
        updatedCaseDetails = new CaseDetails<>();
        beforeCaseDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
    }

    @BeforeAll
    static void createTestValuesAndExpected() {
        final LinkedHashMap<String, Object> folderDocument = createDocumentMap(FOLDER_DOCUMENT_NAME);
        final List<LinkedHashMap<String, Object>> folderDocuments = new ArrayList<>();
        final LinkedHashMap<String, Object> folderDocumentListMap = new LinkedHashMap<>();
        folderDocumentListMap.put("value", folderDocument);
        folderDocuments.add(folderDocumentListMap);

        final LinkedHashMap<String, Object> document = createDocumentMap(DOCUMENT_NAME);
        final List<LinkedHashMap<String, Object>> documents = new ArrayList<>();
        final LinkedHashMap<String, Object> documentListMap = new LinkedHashMap<>();
        documentListMap.put("value", document);
        documents.add(documentListMap);

        final LinkedHashMap<String, Object> document1 = createDocumentMap(DOCUMENT_NAME_1);
        final LinkedHashMap<String, Object> documentListMap1 = new LinkedHashMap<>();
        documentListMap1.put("value", document1);
        final LinkedHashMap<String, Object> document2 = createDocumentMap(DOCUMENT_NAME_2);
        final LinkedHashMap<String, Object> documentListMap2 = new LinkedHashMap<>();
        documentListMap2.put("value", document2);
        final List<LinkedHashMap<String, Object>> multiDocuments = new ArrayList<>();
        multiDocuments.add(documentListMap);
        multiDocuments.add(documentListMap1);
        multiDocuments.add(documentListMap2);

        BUNDLE_LIST_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS = createBundleList(null, null);
        BUNDLE_LIST_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT = createBundleList(null, documents);
        BUNDLE_LIST_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS = createBundleList(null, multiDocuments);
        BUNDLE_LIST_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS = createBundleList(folderDocuments, null);
        BUNDLE_LIST_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS = createBundleList(folderDocuments, documents);
        BUNDLE_LIST_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS = createBundleList(folderDocuments, multiDocuments);
    }

    @ParameterizedTest
    @MethodSource("createBundleObject")
    void shouldCreateBundleSingleFolderAndDocument(LinkedHashMap<String, Object> bundleListMap, Bundle expectedBundle) {
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());

        final List<LinkedHashMap<String, Object>> caseBundles = new ArrayList<>();
        caseBundles.add(bundleListMap);

        final LinkedHashMap<String, Object> caseBundlesMap = new LinkedHashMap<>();
        caseBundlesMap.put("caseBundles", caseBundles);

        final BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(caseBundlesMap);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        final Callback callback = new Callback(updatedCaseDetails, beforeCaseDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);
        final List<Bundle> result = bundlingService.createBundle(bundleCallback);

        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0)).isEqualTo(expectedBundle);
    }

    @Test
    void shouldReturnNullWhenFeignExceptionThrown() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        final FeignException feignException = mock(FeignException.class);
        when(bundlingClient.createBundle(any(), any(), any())).thenThrow(feignException);

        final Callback callback = new Callback(updatedCaseDetails, beforeCaseDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        final List<Bundle> result = bundlingService.createBundle(bundleCallback);

        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).isNull();
    }

    @Test
    void shouldGenerateMultiBundleConfig() {
        final List<MultiBundleConfig> result = bundlingService.getMultiBundleConfigs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue()).isEqualTo(BUNDLE_FILE_NAME);
    }

    @Test
    void shouldGenerateSingleMultiBundleConfig() {
        final MultiBundleConfig result = bundlingService.getMultiBundleConfig();

        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(BUNDLE_FILE_NAME);
    }

    @Test
    void shouldCreateBundleListValues() {
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());

        final LinkedHashMap<String, Object> document = createDocumentMap(DOCUMENT_NAME);
        final List<LinkedHashMap<String, Object>> documents = new ArrayList<>();
        final LinkedHashMap<String, Object> documentListMap = new LinkedHashMap<>();
        documentListMap.put("value", document);
        documents.add(documentListMap);

        final LinkedHashMap<String, Object> bundleListMap = createBundleList(null, documents);

        final List<LinkedHashMap<String, Object>> caseBundles = new ArrayList<>();
        caseBundles.add(bundleListMap);
        //        caseBundles.add(bundleListMap);

        final LinkedHashMap<String, Object> caseBundlesMap = new LinkedHashMap<>();
        caseBundlesMap.put("caseBundles", caseBundles);

        final BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(caseBundlesMap);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        final Callback callback = new Callback(updatedCaseDetails, beforeCaseDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        final List<Bundle> result = bundlingService.createBundle(bundleCallback);
        final List<ListValue<Bundle>> resultList = bundlingService.buildBundleListValues(result);

        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getStitchedDocument()).isNotNull();
        assertThat(result.get(0).getStitchedDocument().getFilename()).isEqualTo("test.pdf");
        assertThat(result.get(0).getDocuments()).hasSize(1);
        assertThat(result.get(0).getDocuments().get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getDocuments().get(0).getValue()).isNotNull();
        assertThat(result.get(0).getDocuments().get(0).getValue().getName()).isEqualTo("name");
        assertThat(result.get(0).getFolders()).isNotNull();
        assertThat(result.get(0).getFolders()).hasSize(1);
        assertThat(result.get(0).getFolders().get(0)).isNotNull();
        assertThat(result.get(0).getFolders().get(0).getId()).isEqualTo("1");
        assertThat(result.get(0).getFolders().get(0).getValue()).isNotNull();
        assertThat(result.get(0).getFolders().get(0).getValue().getName()).isEqualTo("name");
        assertThat(result.get(0).getFolders().get(0).getValue().getDocuments()).isNull();
        assertThat(result.get(0).getFolders().get(0).getValue().getFolders()).isNull();

        assertThat(resultList).isNotNull();

        //        assertThat()
    }

    @Test
    void shouldReturnNullWhenNoBundles() {
        final LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        final BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(linkedHashMap);
        bundleResponse.setErrors(null);
        bundleResponse.setWarnings(null);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        final Callback callback = new Callback(updatedCaseDetails, beforeCaseDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        final List<Bundle> result = bundlingService.createBundle(bundleCallback);
        final List<ListValue<Bundle>> resultList = bundlingService.buildBundleListValues(result);

        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).isEmpty();
        assertThat(resultList).isNull();
    }

    @Test
    void shouldReturnNullWhenNoBundleFolders() {
        final List<BundleFolder> bundleFolderList = Collections.emptyList();
        final List<ListValue<BundleFolder>> resultList = bundlingService.buildBundleFolderListValues(bundleFolderList);
        assertThat(resultList).isNull();
    }

    @Test
    void shouldReturnNullWhenNoBundleDocuments() {
        final List<BundleDocument> bundleDocumentList = Collections.emptyList();
        final List<ListValue<BundleDocument>> resultList = bundlingService.buildBundleDocumentListValues(bundleDocumentList);
        assertThat(resultList).isNull();
    }

    static Stream<Arguments> createBundleObject() {
        final BundleFolder bundleFolder = BundleFolder.builder()
            .name(FOLDER_NAME)
            .sortIndex(1)
            .build();
        final ListValue<BundleFolder> bundleFolderListValueNoDocuments = new ListValue<>();
        bundleFolderListValueNoDocuments.setId("1");
        bundleFolderListValueNoDocuments.setValue(bundleFolder);

        final Bundle bundle1 = createBundle(List.of(bundleFolderListValueNoDocuments), null);

        final BundleDocument bundleDocument = createBundleDocument(DOCUMENT_NAME);

        final ListValue<BundleDocument> bundleDocumentListValue = new ListValue<>();
        bundleDocumentListValue.setId("1");
        bundleDocumentListValue.setValue(bundleDocument);

        final Bundle bundle2 = createBundle(List.of(bundleFolderListValueNoDocuments), List.of(bundleDocumentListValue));

        final ListValue<BundleDocument> bundleDocumentListValue6 = new ListValue<>();
        bundleDocumentListValue6.setId("6");
        bundleDocumentListValue6.setValue(bundleDocument);

        final BundleDocument bundleDocument5 = createBundleDocument(DOCUMENT_NAME_1);

        final ListValue<BundleDocument> bundleDocumentListValue5 = new ListValue<>();
        bundleDocumentListValue5.setId("5");
        bundleDocumentListValue5.setValue(bundleDocument5);

        final BundleDocument bundleDocument4 = createBundleDocument(DOCUMENT_NAME_2);

        final ListValue<BundleDocument> bundleDocumentListValue4 = new ListValue<>();
        bundleDocumentListValue4.setId("4");
        bundleDocumentListValue4.setValue(bundleDocument4);

        final Bundle bundle3 = createBundle(List.of(bundleFolderListValueNoDocuments),
                                            List.of(bundleDocumentListValue4, bundleDocumentListValue5, bundleDocumentListValue6));

        final BundleDocument bundleFolderDocument = createBundleDocument(FOLDER_DOCUMENT_NAME);

        final ListValue<BundleDocument> bundleFolderDocumentListValue = new ListValue<>();
        bundleFolderDocumentListValue.setId("1");
        bundleFolderDocumentListValue.setValue(bundleFolderDocument);

        final BundleFolder bundleFolderOneDoc = BundleFolder.builder()
            .name(FOLDER_NAME)
            .documents(List.of(bundleFolderDocumentListValue))
            .sortIndex(1)
            .build();
        final ListValue<BundleFolder> bundleFolderListValueOneDocuments = new ListValue<>();
        bundleFolderListValueOneDocuments.setId("1");
        bundleFolderListValueOneDocuments.setValue(bundleFolderOneDoc);

        final Bundle bundle4 = createBundle(List.of(bundleFolderListValueOneDocuments), null);
        final Bundle bundle5 = createBundle(List.of(bundleFolderListValueOneDocuments), List.of(bundleDocumentListValue));
        final Bundle bundle6 = createBundle(List.of(bundleFolderListValueOneDocuments),
                                            List.of(bundleDocumentListValue4, bundleDocumentListValue5, bundleDocumentListValue6));

        return Stream.of(
            Arguments.arguments(BUNDLE_LIST_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS, bundle1),
            Arguments.arguments(BUNDLE_LIST_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT, bundle2),
            Arguments.arguments(BUNDLE_LIST_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS, bundle3),
            Arguments.arguments(BUNDLE_LIST_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS, bundle4),
            Arguments.arguments(BUNDLE_LIST_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS, bundle5),
            Arguments.arguments(BUNDLE_LIST_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS, bundle6)
        );
    }

    private static BundleDocument createBundleDocument(String name) {
        return BundleDocument.builder()
            .name(name)
            .sortIndex(1)
            .description(DESCRIPTION)
            .build();
    }

    private static LinkedHashMap<String, Object> createBundleList(List<LinkedHashMap<String, Object>> folderDocuments,
                                                                  List<LinkedHashMap<String, Object>> documents) {
        final LinkedHashMap<String, Object> bundleMap = new LinkedHashMap<>();
        bundleMap.put("id", "1");
        bundleMap.put("paginationStyle", BundlePaginationStyle.off);
        bundleMap.put("pageNumberFormat", PageNumberFormat.numberOfPages);
        bundleMap.put("stitchingFailureMessage", "1");
        bundleMap.put("stitchingStatus", "1");

        final LinkedHashMap<String, Object> stitchedDocMap = createStitchedDocMap();
        bundleMap.put("stitchedDocument", stitchedDocMap);

        final LinkedHashMap<String, Object> folder = createFolderMap(folderDocuments);
        final List<LinkedHashMap<String, Object>> folders = new ArrayList<>();
        final LinkedHashMap<String, Object> folderListMap = new LinkedHashMap<>();
        folderListMap.put("value", folder);
        folders.add(folderListMap);
        bundleMap.put("folders", folders);

        bundleMap.put("documents", documents);

        final LinkedHashMap<String, Object> bundleListMap = new LinkedHashMap<>();
        bundleListMap.put("value", bundleMap);
        return bundleListMap;
    }

    private static Bundle createBundle(List<ListValue<BundleFolder>> bundleFolders, List<ListValue<BundleDocument>> bundleDocuments) {
        return Bundle.builder()
            .id("1")
            .title("")
            .description("")
            .stitchedDocument(Document.builder()
                .url("http://url/documents/id")
                .filename("test.pdf")
                .binaryUrl("http://url/documents/id")
                .build())
            .folders(bundleFolders)
            .documents(bundleDocuments)
            .paginationStyle(BundlePaginationStyle.off)
            .pageNumberFormat(PageNumberFormat.numberOfPages)
            .stitchingFailureMessage("1")
            .stitchStatus("1")
            .build();
    }

    private static LinkedHashMap<String, Object> createStitchedDocMap() {
        final LinkedHashMap<String, Object> stitchedDocMap = new LinkedHashMap<>();
        stitchedDocMap.put("document_url", "http://url/documents/id");
        stitchedDocMap.put("document_filename", "test.pdf");
        stitchedDocMap.put("document_binary_url", "http://url/documents/id");
        return stitchedDocMap;
    }

    private static LinkedHashMap<String, Object> createDocumentMap(String name) {
        final LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put("name", name);
        document.put("sortIndex", 1);
        document.put("description", DESCRIPTION);
        return document;
    }

    private static LinkedHashMap<String, Object> createFolderMap(List<LinkedHashMap<String, Object>> folderDocumentsList) {
        final LinkedHashMap<String, Object> folder = new LinkedHashMap<>();
        folder.put("name", FOLDER_NAME);
        folder.put("sortIndex", 1);
        folder.put("documents", folderDocumentsList);
        return folder;
    }
}
