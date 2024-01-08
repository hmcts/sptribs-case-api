package uk.gov.hmcts.sptribs.document.bundling;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
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

    @InjectMocks
    private BundlingService bundlingService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private BundlingClient bundlingClient;

    @Test
    void shouldCreateBundle() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());

        LinkedHashMap<String, Object> bundleMap = new LinkedHashMap<>();
        bundleMap.put("id", "1");
        bundleMap.put("paginationStyle", BundlePaginationStyle.off);
        bundleMap.put("pageNumberFormat", PageNumberFormat.numberOfPages);
        bundleMap.put("stitchingFailureMessage", "1");
        bundleMap.put("stitchingStatus", "1");
        LinkedHashMap<String, Object> stitchedDocMap = new LinkedHashMap<>();
        stitchedDocMap.put("document_url", "http://url/documents/id");
        stitchedDocMap.put("document_filename", "test.pdf");
        stitchedDocMap.put("document_binary_url", "http://url/documents/id");
        bundleMap.put("stitchedDocument", stitchedDocMap);

        LinkedHashMap<String, Object> folder = new LinkedHashMap<>();
        folder.put("name", "name");
        folder.put("sortIndex", 1);
        folder.put("documents", null);
        List<LinkedHashMap<String, Object>> folders = new ArrayList<>();
        LinkedHashMap<String, Object> folderListMap = new LinkedHashMap<>();
        folderListMap.put("value", folder);
        folders.add(folderListMap);
        bundleMap.put("folders", folders);

        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put("name", "name");
        document.put("sortIndex", 1);
        document.put("description", "null");
        List<LinkedHashMap<String, Object>> documents = new ArrayList<>();
        LinkedHashMap<String, Object> documentListMap = new LinkedHashMap<>();
        documentListMap.put("value", document);
        documents.add(documentListMap);
        bundleMap.put("documents", documents);

        LinkedHashMap<String, Object> bundleListMap = new LinkedHashMap<>();
        bundleListMap.put("value", bundleMap);
        List<LinkedHashMap<String, Object>> caseBundles = new ArrayList<>();
        caseBundles.add(bundleListMap);

        LinkedHashMap<String, Object> caseBundleMap = new LinkedHashMap<>();
        caseBundleMap.put("caseBundles", caseBundles);
        BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(caseBundleMap);
        bundleResponse.setErrors(null);
        bundleResponse.setWarnings(null);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        Callback callback = new Callback(updatedCaseDetails, beforeDetails, CREATE_BUNDLE, true);
        BundleCallback bundleCallback = new BundleCallback(callback);
        //When
        List<Bundle> result = bundlingService.createBundle(bundleCallback);

        //Then
        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnNullWhenFeignExceptionThrown() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        final FeignException feignException = mock(FeignException.class);
        when(bundlingClient.createBundle(any(), any(), any())).thenThrow(feignException);

        Callback callback = new Callback(updatedCaseDetails, beforeDetails, CREATE_BUNDLE, true);
        BundleCallback bundleCallback = new BundleCallback(callback);
        //When
        List<Bundle> result = bundlingService.createBundle(bundleCallback);

        //Then
        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).isNull();
    }

    @Test
    void shouldGenerateMultiBundleConfig() {
        //When
        List<MultiBundleConfig> result = bundlingService.getMultiBundleConfigs();

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isNotNull();
    }

    @Test
    void shouldCreateBundleListValues() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());

        LinkedHashMap<String, Object> bundleMap = new LinkedHashMap<>();
        bundleMap.put("id", "1");
        bundleMap.put("paginationStyle", BundlePaginationStyle.off);
        bundleMap.put("pageNumberFormat", PageNumberFormat.numberOfPages);
        bundleMap.put("stitchingFailureMessage", "1");
        bundleMap.put("stitchingStatus", "1");

        LinkedHashMap<String, Object> stitchedDocMap = new LinkedHashMap<>();
        stitchedDocMap.put("document_url", "http://url/documents/id");
        stitchedDocMap.put("document_filename", "test.pdf");
        stitchedDocMap.put("document_binary_url", "http://url/documents/id");
        bundleMap.put("stitchedDocument", stitchedDocMap);

        LinkedHashMap<String, Object> folder = new LinkedHashMap<>();
        folder.put("name", "name");
        folder.put("sortIndex", 1);
        folder.put("documents", null);
        List<LinkedHashMap<String, Object>> folders = new ArrayList<>();
        LinkedHashMap<String, Object> folderListMap = new LinkedHashMap<>();
        folderListMap.put("value", folder);
        folders.add(folderListMap);
        bundleMap.put("folders", folders);

        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put("name", "name");
        document.put("sortIndex", 1);
        document.put("description", "null");
        List<LinkedHashMap<String, Object>> documents = new ArrayList<>();
        LinkedHashMap<String, Object> documentListMap = new LinkedHashMap<>();
        documentListMap.put("value", document);
        documents.add(documentListMap);
        bundleMap.put("documents", documents);

        LinkedHashMap<String, Object> bundleListMap = new LinkedHashMap<>();
        bundleListMap.put("value", bundleMap);
        List<LinkedHashMap<String, Object>> caseBundles = new ArrayList<>();
        caseBundles.add(bundleListMap);

        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("caseBundles", caseBundles);
        BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(linkedHashMap);
        bundleResponse.setErrors(null);
        bundleResponse.setWarnings(null);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        Callback callback = new Callback(updatedCaseDetails, beforeDetails, CREATE_BUNDLE, true);
        BundleCallback bundleCallback = new BundleCallback(callback);
        //When
        List<Bundle> result = bundlingService.createBundle(bundleCallback);
        List<ListValue<Bundle>> resultList = bundlingService.buildBundleListValues(result);

        //Then
        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).isNotNull();
        assertThat(resultList).isNotNull();
    }

    @Test
    void shouldReturnNullWhenNoBundles() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        BundleResponse bundleResponse = new BundleResponse();
        bundleResponse.setData(linkedHashMap);
        bundleResponse.setErrors(null);
        bundleResponse.setWarnings(null);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(bundlingClient.createBundle(any(), any(), any())).thenReturn(bundleResponse);

        Callback callback = new Callback(updatedCaseDetails, beforeDetails, CREATE_BUNDLE, true);
        BundleCallback bundleCallback = new BundleCallback(callback);
        //When
        List<Bundle> result = bundlingService.createBundle(bundleCallback);
        List<ListValue<Bundle>> resultList = bundlingService.buildBundleListValues(result);

        //Then
        verify(bundlingClient).createBundle(any(), any(), any());
        assertThat(result).isEmpty();
        assertThat(resultList).isNull();
    }

    @Test
    void shouldReturnNullWhenNoBundleFolders() {
        //Given
        List<BundleFolder> bundleFolderList = Collections.emptyList();
        //When
        List<ListValue<BundleFolder>> resultList = bundlingService.buildBundleFolderListValues(bundleFolderList);
        //Then
        assertThat(resultList).isNull();
    }

    @Test
    void shouldReturnNullWhenNoBundleDocuments() {
        //Given
        List<BundleDocument> bundleDocumentList = Collections.emptyList();
        //When
        List<ListValue<BundleDocument>> resultList = bundlingService.buildBundleDocumentListValues(bundleDocumentList);
        //Then
        assertThat(resultList).isNull();
    }


}
