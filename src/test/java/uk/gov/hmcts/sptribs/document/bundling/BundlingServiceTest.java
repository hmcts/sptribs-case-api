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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DESCRIPTION;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENTS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENT_URL;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.FOLDERS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.ID;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.NAME;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.PAGE_NUMBER_FORMAT;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.PAGINATION_STYLE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.SORT_INDEX;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.STITCHING_FAILURE_MESSAGE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.STITCHING_STATUS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class BundlingServiceTest {

    public static final String BUNDLE_FILE_NAME = "st_cic_bundle_all_case.yaml";
    public static final String FOLDER_DOCUMENT_NAME = "folderDocument";
    public static final String FOLDER_NAME = "folderName";
    public static final String DOCUMENT_NAME = "documentName";
    public static final String DOCUMENT_NAME_1 = "documentName1";
    public static final String DOCUMENT_NAME_2 = "documentName2";
    public static LinkedHashMap<String, Object> DEFAULT_BUNDLE_MAP;
    public static LinkedHashMap<String, Object> DEFAULT_BUNDLE_MAP_NULL_FOLDER;
    public static LinkedHashMap<String, Object> BUNDLE_MAP_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_MAP_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT;
    public static LinkedHashMap<String, Object> BUNDLE_MAP_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_MAP_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_MAP_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS;
    public static LinkedHashMap<String, Object> BUNDLE_MAP_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS;
    public static Bundle BUNDLE_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS;
    public static Bundle BUNDLE_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT;
    public static Bundle BUNDLE_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS;
    public static Bundle BUNDLE_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS;
    public static Bundle BUNDLE_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS;
    public static Bundle BUNDLE_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS;
    public static Bundle DEFAULT_BUNDLE;
    public static Bundle DEFAULT_BUNDLE_NULL_FOLDER;

    @InjectMocks
    private BundlingService bundlingService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private BundlingClient bundlingClient;

    @Mock
    private Clock clock;

    private CaseData caseData;

    private CaseDetails<CaseData, State> updatedCaseDetails;

    private CaseDetails<CaseData, State> beforeCaseDetails;

    private static final Instant instant = Instant.now();
    private static final ZoneId zoneId = ZoneId.systemDefault();

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
        createBundleListMaps();
        createBundleObjects();
    }

    @ParameterizedTest
    @MethodSource("createBundleTestValues")
    void shouldCreateBundle(LinkedHashMap<String, Object> bundleListMap, Bundle expectedBundle) {
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

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        final Callback callback = new Callback(updatedCaseDetails, beforeCaseDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);
        final List<Bundle> result = bundlingService.createBundle(bundleCallback);

        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0)).isEqualTo(expectedBundle);
        assertThat(result.get(0).getDocuments()).isEqualTo(expectedBundle.getDocuments());
        assertThat(result.get(0).getFolders()).isEqualTo(expectedBundle.getFolders());
        assertThat(result.get(0).getStitchedDocument()).isEqualTo(expectedBundle.getStitchedDocument());
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

    @ParameterizedTest
    @MethodSource("createBundleListTestValues")
    void shouldCreateBundleListValues(List<LinkedHashMap<String, Object>> bundleListMapList, List<Bundle> expectedBundles) {
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());

        final List<LinkedHashMap<String, Object>> caseBundles = new ArrayList<>(bundleListMapList);

        final LinkedHashMap<String, Object> caseBundlesMap = new LinkedHashMap<>();
        caseBundlesMap.put("caseBundles", caseBundles);

        final BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(caseBundlesMap);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        final Callback callback = new Callback(updatedCaseDetails, beforeCaseDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        final List<Bundle> result = bundlingService.createBundle(bundleCallback);
        final List<ListValue<Bundle>> resultList = bundlingService.buildBundleListValues(result);

        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).hasSize(expectedBundles.size()).containsAll(expectedBundles);
        assertThat(resultList.stream().map(ListValue::getValue).toList()).containsAll(expectedBundles);
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

    static Stream<Arguments> createBundleTestValues() {
        return Stream.of(
            Arguments.arguments(DEFAULT_BUNDLE_MAP, DEFAULT_BUNDLE),
            Arguments.arguments(DEFAULT_BUNDLE_MAP_NULL_FOLDER, DEFAULT_BUNDLE_NULL_FOLDER),
            Arguments.arguments(BUNDLE_MAP_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS, BUNDLE_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS),
            Arguments.arguments(BUNDLE_MAP_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT, BUNDLE_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT),
            Arguments.arguments(BUNDLE_MAP_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS, BUNDLE_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS),
            Arguments.arguments(BUNDLE_MAP_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS, BUNDLE_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS),
            Arguments.arguments(BUNDLE_MAP_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS, BUNDLE_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS),
            Arguments.arguments(BUNDLE_MAP_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS, BUNDLE_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS)
        );
    }

    static Stream<Arguments> createBundleListTestValues() {
        final List<LinkedHashMap<String, Object>> listOfNullFolderDocumentBundle = List.of(BUNDLE_MAP_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS,
            BUNDLE_MAP_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT, BUNDLE_MAP_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS);
        final List<LinkedHashMap<String, Object>> listOfFolderDocumentsBundle = List.of(BUNDLE_MAP_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS,
            BUNDLE_MAP_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS, BUNDLE_MAP_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS);
        final List<Bundle> nullFolderDocumentBundleList = List.of(BUNDLE_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS,
            BUNDLE_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT, BUNDLE_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS);
        final List<Bundle> oneFolderDocumentBundleList = List.of(BUNDLE_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS,
            BUNDLE_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS, BUNDLE_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS);
        return Stream.of(
            Arguments.arguments(List.of(BUNDLE_MAP_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS),
                List.of(BUNDLE_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS)),
            Arguments.arguments(List.of(BUNDLE_MAP_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS),
                List.of(BUNDLE_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS)),
            Arguments.arguments(listOfNullFolderDocumentBundle, nullFolderDocumentBundleList),
            Arguments.arguments(listOfFolderDocumentsBundle, oneFolderDocumentBundleList)
        );
    }

    private static void createBundleObjects() {
        final BundleFolder defaultBundleFolder = BundleFolder.builder().name("").build();
        final ListValue<BundleFolder> defaultBundleFolderListValue = new ListValue<>();
        defaultBundleFolderListValue.setId("1");
        defaultBundleFolderListValue.setValue(defaultBundleFolder);

        final BundleFolder bundleFolder = BundleFolder.builder()
            .name(FOLDER_NAME)
            .sortIndex(1)
            .build();
        final ListValue<BundleFolder> bundleFolderListValueNoDocuments = new ListValue<>();
        bundleFolderListValueNoDocuments.setId("1");
        bundleFolderListValueNoDocuments.setValue(bundleFolder);

        DEFAULT_BUNDLE = Bundle.builder()
            .id("")
            .dateAndTime(LocalDateTime.now(Clock.fixed(
                instant,
                ZoneOffset.UTC)))
            .description("")
            .title("")
            .stitchingFailureMessage("")
            .stitchStatus("")
            .folders(List.of(defaultBundleFolderListValue))
            .build();

        DEFAULT_BUNDLE_NULL_FOLDER = Bundle.builder()
            .id("")
            .dateAndTime(LocalDateTime.now(Clock.fixed(
                instant,
                ZoneOffset.UTC)))
            .description("")
            .title("")
            .stitchingFailureMessage("")
            .stitchStatus("")
            .folders(null)
            .build();

        final BundleDocument bundleDocument = createBundleDocument(DOCUMENT_NAME);

        final ListValue<BundleDocument> bundleDocumentListValue = new ListValue<>();
        bundleDocumentListValue.setId("1");
        bundleDocumentListValue.setValue(bundleDocument);


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

        final Document stitchedDocument = Document.builder()
            .url("http://url/documents/id")
            .filename("test.pdf")
            .binaryUrl("http://url/documents/id")
            .build();

        BUNDLE_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS =
            createBundle(null, List.of(bundleFolderListValueNoDocuments), null);
        BUNDLE_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT =
            createBundle(null, List.of(bundleFolderListValueNoDocuments), List.of(bundleDocumentListValue));
        BUNDLE_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS =
            createBundle(null, List.of(bundleFolderListValueNoDocuments),
                List.of(bundleDocumentListValue4, bundleDocumentListValue5, bundleDocumentListValue6));
        BUNDLE_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS =
            createBundle(stitchedDocument, List.of(bundleFolderListValueOneDocuments), null);
        BUNDLE_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS =
            createBundle(stitchedDocument, List.of(bundleFolderListValueOneDocuments), List.of(bundleDocumentListValue));
        BUNDLE_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS =
            createBundle(stitchedDocument, List.of(bundleFolderListValueOneDocuments),
                List.of(bundleDocumentListValue4, bundleDocumentListValue5, bundleDocumentListValue6));
    }

    private static void createBundleListMaps() {
        final List<LinkedHashMap<String, Object>> emptyFolders = new ArrayList<>();
        final LinkedHashMap<String, Object> emptyFolder = new LinkedHashMap<>();
        final LinkedHashMap<String, Object> emptyFolderListMap = new LinkedHashMap<>();
        emptyFolderListMap.put("value", emptyFolder);
        emptyFolders.add(emptyFolderListMap);
        final LinkedHashMap<String, Object> emptyBundleMap = new LinkedHashMap<>();
        emptyBundleMap.put(FOLDERS, emptyFolders);
        DEFAULT_BUNDLE_MAP = new LinkedHashMap<>();
        DEFAULT_BUNDLE_MAP.put("value", emptyBundleMap);

        final LinkedHashMap<String, Object> nullFoldersBundleMap = new LinkedHashMap<>();
        nullFoldersBundleMap.put(FOLDERS, null);
        DEFAULT_BUNDLE_MAP_NULL_FOLDER = new LinkedHashMap<>();
        DEFAULT_BUNDLE_MAP_NULL_FOLDER.put("value", nullFoldersBundleMap);

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

        final LinkedHashMap<String, Object> stitchedDocMap = createStitchedDocMap();

        BUNDLE_MAP_NULL_FOLDER_DOCUMENT_NULL_DOCUMENTS = createBundleList(null,null, null);
        BUNDLE_MAP_NULL_FOLDER_DOCUMENT_ONE_DOCUMENT = createBundleList(null, null, documents);
        BUNDLE_MAP_NULL_FOLDER_DOCUMENT_MULTI_DOCUMENTS = createBundleList(null, null, multiDocuments);
        BUNDLE_MAP_ONE_FOLDER_DOCUMENT_NULL_DOCUMENTS = createBundleList(stitchedDocMap, folderDocuments, null);
        BUNDLE_MAP_ONE_FOLDER_DOCUMENT_ONE_DOCUMENTS = createBundleList(stitchedDocMap, folderDocuments, documents);
        BUNDLE_MAP_ONE_FOLDER_DOCUMENT_MULTI_DOCUMENTS = createBundleList(stitchedDocMap, folderDocuments, multiDocuments);
    }

    private static BundleDocument createBundleDocument(String name) {
        return BundleDocument.builder()
            .name(name)
            .sortIndex(1)
            .description("some description")
            .build();
    }

    private static LinkedHashMap<String, Object> createBundleList(LinkedHashMap<String, Object> stitchedDocuments,
                                                                  List<LinkedHashMap<String, Object>> folderDocuments,
                                                                  List<LinkedHashMap<String, Object>> documents) {
        final LinkedHashMap<String, Object> bundleMap = new LinkedHashMap<>();
        bundleMap.put(ID, "1");
        bundleMap.put(PAGINATION_STYLE, BundlePaginationStyle.off);
        bundleMap.put(PAGE_NUMBER_FORMAT, PageNumberFormat.numberOfPages);
        bundleMap.put(STITCHING_FAILURE_MESSAGE, "1");
        bundleMap.put(STITCHING_STATUS, "1");

        bundleMap.put("stitchedDocument", stitchedDocuments);

        final LinkedHashMap<String, Object> folder = createFolderMap(folderDocuments);
        final List<LinkedHashMap<String, Object>> folders = new ArrayList<>();
        final LinkedHashMap<String, Object> folderListMap = new LinkedHashMap<>();
        folderListMap.put("value", folder);
        folders.add(folderListMap);
        bundleMap.put(FOLDERS, folders);

        bundleMap.put(DOCUMENTS, documents);

        final LinkedHashMap<String, Object> bundleListMap = new LinkedHashMap<>();
        bundleListMap.put("value", bundleMap);
        return bundleListMap;
    }

    private static Bundle createBundle(Document stitchedDocument,
                                       List<ListValue<BundleFolder>> bundleFolders,
                                       List<ListValue<BundleDocument>> bundleDocuments) {
        return Bundle.builder()
            .id("1")
            .dateAndTime(LocalDateTime.now(Clock.fixed(
                instant,
                ZoneOffset.UTC)))
            .title("")
            .description("")
            .stitchedDocument(stitchedDocument)
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
        stitchedDocMap.put(DOCUMENT_URL, "http://url/documents/id");
        stitchedDocMap.put(DOCUMENT_FILENAME, "test.pdf");
        stitchedDocMap.put(DOCUMENT_BINARY_URL, "http://url/documents/id");
        return stitchedDocMap;
    }

    private static LinkedHashMap<String, Object> createDocumentMap(String name) {
        final LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put(NAME, name);
        document.put(SORT_INDEX, 1);
        document.put(DESCRIPTION, "some description");
        return document;
    }

    private static LinkedHashMap<String, Object> createFolderMap(List<LinkedHashMap<String, Object>> folderDocumentsList) {
        final LinkedHashMap<String, Object> folder = new LinkedHashMap<>();
        folder.put(NAME, FOLDER_NAME);
        folder.put(SORT_INDEX, 1);
        folder.put(DOCUMENTS, folderDocumentsList);
        return folder;
    }
}
