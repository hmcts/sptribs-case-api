package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingService;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.MultiBundleConfig;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CREATE_BUNDLE;

@ExtendWith(MockitoExtension.class)
class CaseworkerCreateBundleTest {

    private static final MultiBundleConfig MULTI_BUNDLE_CONFIG = MultiBundleConfig.builder().value("st_cic_bundle_all_case.yaml").build();

    @InjectMocks
    private CaseworkerCreateBundle caseworkerCreateBundle;

    @Mock
    private BundlingService bundlingService;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCreateBundle.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CREATE_BUNDLE);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    public void shouldSuccessfullyCreateBundle() {
        final CaseData caseData = caseData();
        final List<ListValue<CaseworkerCICDocument>> cicDocuments = getCaseworkerCICDocumentList();
        final CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(cicDocuments);
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Bundle bundle = Bundle.builder().build();

        when(bundlingService.getMultiBundleConfig()).thenCallRealMethod();
        when(bundlingService.getMultiBundleConfigs()).thenCallRealMethod();

        when(bundlingService.createBundle(any(BundleCallback.class))).thenAnswer(callback -> {
            final BundleCallback callbackAtMockTime = (BundleCallback) callback.getArguments()[0];

            //check case data at call time
            final CaseData dataAtMockTime = callbackAtMockTime.getCaseDetails().getData();
            assertThat(dataAtMockTime.getCaseDocuments().getFirst().getValue()).isEqualTo(cicDocuments.getFirst().getValue());
            assertThat(dataAtMockTime.getBundleConfiguration()).isEqualTo(MULTI_BUNDLE_CONFIG);
            assertThat(dataAtMockTime.getMultiBundleConfiguration()).isEqualTo(List.of(MULTI_BUNDLE_CONFIG));
            return List.of(bundle);
        });

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCreateBundle.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        verify(bundlingService).getMultiBundleConfig();
        verify(bundlingService).getMultiBundleConfigs();
        verify(bundlingService).buildBundleListValues(anyList());

        final CaseData responseData = response.getData();
        assertThat(responseData)
            .isNotNull()
            .isEqualTo(updatedCaseDetails.getData());
        assertThat(responseData.getCaseBundles()).isNotNull();

        //case documents should remain null so that they are not duplicated
        //i.e. not in their respective child objects as well (CicCase.applicantDocumentsUploaded)
        assertThat(responseData.getCaseDocuments()).isNull();
        assertThat(responseData.getMultiBundleConfiguration()).isNull();
    }

    @Test
    public void shouldSuccessfullyCreateBundleWithNewOrderEnabled() {
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.YES);

        // Set up initial CICA documents
        final List<ListValue<CaseworkerCICDocument>> initialDocuments = getCaseworkerCICDocumentList("initial.pdf");
        caseData.setInitialCicaDocuments(initialDocuments);

        // Set up further uploaded documents
        final List<ListValue<CaseworkerCICDocument>> furtherDocuments = getCaseworkerCICDocumentList("further.pdf");
        caseData.setFurtherUploadedDocuments(furtherDocuments);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Bundle bundle = Bundle.builder().build();

        when(bundlingService.getMultiBundleConfig()).thenCallRealMethod();
        when(bundlingService.getMultiBundleConfigs()).thenCallRealMethod();

        when(bundlingService.createBundle(any(BundleCallback.class))).thenAnswer(callback -> {
            final BundleCallback callbackAtMockTime = (BundleCallback) callback.getArguments()[0];

            //check case data at call time
            final CaseData dataAtMockTime = callbackAtMockTime.getCaseDetails().getData();
            // Should have initial documents in caseDocuments
            assertThat(dataAtMockTime.getCaseDocuments()).hasSize(1);
            assertThat(dataAtMockTime.getCaseDocuments().getFirst().getValue()).isEqualTo(initialDocuments.getFirst().getValue());
            // Should have further documents in furtherCaseDocuments
            assertThat(dataAtMockTime.getFurtherCaseDocuments()).hasSize(1);
            assertThat(dataAtMockTime.getFurtherCaseDocuments().getFirst().getValue()).isEqualTo(furtherDocuments.getFirst().getValue());
            assertThat(dataAtMockTime.getBundleConfiguration()).isEqualTo(MULTI_BUNDLE_CONFIG);
            assertThat(dataAtMockTime.getMultiBundleConfiguration()).isEqualTo(List.of(MULTI_BUNDLE_CONFIG));
            return List.of(bundle);
        });

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCreateBundle.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        verify(bundlingService).getMultiBundleConfig();
        verify(bundlingService).getMultiBundleConfigs();
        verify(bundlingService).buildBundleListValues(anyList());

        final CaseData responseData = response.getData();
        assertThat(responseData)
            .isNotNull()
            .isEqualTo(updatedCaseDetails.getData());
        assertThat(responseData.getCaseBundles()).isNotNull();

        //case documents should remain null so that they are not duplicated
        //i.e. not in their respective child objects as well (CicCase.applicantDocumentsUploaded)
        assertThat(responseData.getCaseDocuments()).isNull();
        assertThat(responseData.getMultiBundleConfiguration()).isNull();
    }

    @Test
    public void shouldUseOldBundleLogicWhenNewOrderDisabled() {
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.NO);

        final List<ListValue<CaseworkerCICDocument>> cicDocuments = getCaseworkerCICDocumentList("test.pdf");
        final CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(cicDocuments);
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Bundle bundle = Bundle.builder().build();

        when(bundlingService.getMultiBundleConfig()).thenCallRealMethod();
        when(bundlingService.getMultiBundleConfigs()).thenCallRealMethod();

        when(bundlingService.createBundle(any(BundleCallback.class))).thenAnswer(callback -> {
            final BundleCallback callbackAtMockTime = (BundleCallback) callback.getArguments()[0];

            final CaseData dataAtMockTime = callbackAtMockTime.getCaseDetails().getData();
            // Should use old logic - documents should be in caseDocuments
            assertThat(dataAtMockTime.getCaseDocuments()).hasSize(1);
            assertThat(dataAtMockTime.getCaseDocuments().getFirst().getValue()).isEqualTo(cicDocuments.getFirst().getValue());
            // furtherCaseDocuments should be null when new order is disabled
            assertThat(dataAtMockTime.getFurtherCaseDocuments()).isNull();
            assertThat(dataAtMockTime.getBundleConfiguration()).isEqualTo(MULTI_BUNDLE_CONFIG);
            assertThat(dataAtMockTime.getMultiBundleConfiguration()).isEqualTo(List.of(MULTI_BUNDLE_CONFIG));
            return List.of(bundle);
        });

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCreateBundle.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        verify(bundlingService).getMultiBundleConfig();
        verify(bundlingService).getMultiBundleConfigs();
        verify(bundlingService).buildBundleListValues(anyList());

        final CaseData responseData = response.getData();
        assertThat(responseData)
            .isNotNull()
            .isEqualTo(updatedCaseDetails.getData());
        assertThat(responseData.getCaseBundles()).isNotNull();
        assertThat(responseData.getCaseDocuments()).isNull();
        assertThat(responseData.getMultiBundleConfiguration()).isNull();
    }

    @Test
    public void shouldIgnoreInvalidFilesWhenCreatingBundle() {
        final CaseData caseData = caseData();
        final List<ListValue<CaseworkerCICDocument>> documents = getCaseworkerCICDocumentList("test.mp3");
        final CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(documents);
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Bundle bundle = Bundle.builder().build();

        when(bundlingService.getMultiBundleConfig()).thenCallRealMethod();
        when(bundlingService.getMultiBundleConfigs()).thenCallRealMethod();

        when(bundlingService.createBundle(any(BundleCallback.class))).thenAnswer(callback -> {
            final BundleCallback callbackAtMockTime = (BundleCallback) callback.getArguments()[0];

            final CaseData dataAtMockTime = callbackAtMockTime.getCaseDetails().getData();
            assertThat(dataAtMockTime.getCaseDocuments()).isEmpty();
            assertThat(dataAtMockTime.getBundleConfiguration()).isEqualTo(MULTI_BUNDLE_CONFIG);
            assertThat(dataAtMockTime.getMultiBundleConfiguration()).isEqualTo(List.of(MULTI_BUNDLE_CONFIG));
            return List.of(bundle);
        });

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCreateBundle.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        verify(bundlingService).getMultiBundleConfig();
        verify(bundlingService).getMultiBundleConfigs();
        verify(bundlingService).buildBundleListValues(anyList());

        final CaseData responseData = response.getData();
        assertThat(responseData)
            .isNotNull()
            .isEqualTo(updatedCaseDetails.getData());
        assertThat(responseData.getCaseBundles()).isNotNull();
        assertThat(responseData.getCaseDocuments()).isNull();
        assertThat(responseData.getMultiBundleConfiguration()).isNull();
    }


}
