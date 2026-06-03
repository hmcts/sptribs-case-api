package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectBundles;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleIdAndTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.REMOVE_BUNDLES;

@ExtendWith(MockitoExtension.class)
class CaseworkerRemoveBundlesTest {

    @InjectMocks
    private CaseworkerRemoveBundles caseworkerRemoveBundles;

    @InjectMocks
    private SelectBundles selectBundles;

    private CaseData caseData;

    private CaseDetails<CaseData, State> updatedCaseDetails;

    @BeforeEach
    void setUp() {
        caseData = caseData();
        updatedCaseDetails = new CaseDetails<>();

        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        String bundleUUID1 = UUID.randomUUID().toString();
        String bundleUUID2 = UUID.randomUUID().toString();
        String bundleUUID3 = UUID.randomUUID().toString();

        final LocalDateTime bundleTimestamp1 = LocalDateTime.now();
        final LocalDateTime bundleTimestamp2 = LocalDateTime.now().minusDays(1);
        final LocalDateTime bundleTimestamp3 = LocalDateTime.now().minusDays(2);

        Document stitchedDocument1 = Document.builder()
            .filename("1-cicBundle.pdf")
            .build();
        Document stitchedDocument2 = Document.builder()
            .filename("2-cicBundle.pdf")
            .build();
        Document stitchedDocument3 = Document.builder()
            .filename("3-cicBundle.pdf")
            .build();

        List<ListValue<Bundle>> existingBundles = new ArrayList<>();
        existingBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder()
                .id(bundleUUID1)
                .dateAndTime(bundleTimestamp1)
                .stitchedDocument(stitchedDocument1)
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("2")
            .value(Bundle.builder()
                .id(bundleUUID2)
                .dateAndTime(bundleTimestamp2)
                .stitchedDocument(stitchedDocument2)
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("3")
            .value(Bundle.builder()
                .id(bundleUUID3)
                .dateAndTime(bundleTimestamp3)
                .stitchedDocument(stitchedDocument3)
                .build())
            .build());

        caseData.setCaseBundles(existingBundles);

        List<ListValue<BundleIdAndTimestamp>> bundleIdAndTimestamps = new ArrayList<>();
        bundleIdAndTimestamps.add(ListValue.<BundleIdAndTimestamp>builder()
            .id("1")
            .value(BundleIdAndTimestamp.builder()
                .bundleId(bundleUUID1)
                .dateAndTime(bundleTimestamp1)
                .build())
            .build());
        bundleIdAndTimestamps.add(ListValue.<BundleIdAndTimestamp>builder()
            .id("2")
            .value(BundleIdAndTimestamp.builder()
                .bundleId(bundleUUID2)
                .dateAndTime(bundleTimestamp2)
                .build())
            .build());
        bundleIdAndTimestamps.add(ListValue.<BundleIdAndTimestamp>builder()
            .id("3")
            .value(BundleIdAndTimestamp.builder()
                .bundleId(bundleUUID3)
                .dateAndTime(bundleTimestamp3)
                .build())
            .build());

        caseData.setCaseBundleIdsAndTimestamps(bundleIdAndTimestamps);

        updatedCaseDetails.setData(caseData);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveBundles.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(REMOVE_BUNDLES);
    }

    @Test
    void shouldSuccessfullyPrepareBundleLabelsOnAboutToStart() {
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveBundles.aboutToStart(updatedCaseDetails);

        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems())
            .hasSize(3);
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems().getFirst().getLabel())
            .isEqualTo(caseData.getCaseBundles().getFirst().getValue().getDateAndTime() + " -- 1-cicBundle.pdf");
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems().get(1).getLabel())
            .isEqualTo(caseData.getCaseBundles().get(1).getValue().getDateAndTime() + " -- 2-cicBundle.pdf");
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems().get(2).getLabel())
            .isEqualTo(caseData.getCaseBundles().get(2).getValue().getDateAndTime() + " -- 3-cicBundle.pdf");
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getValue()).isNull();
    }

    @Test
    void shouldSuccessfullyPrepareBundleLabelsOnAboutToStartWhenStitchedDocumentIsNull() {
        updatedCaseDetails.getData().getCaseBundles().getFirst().getValue().setStitchedDocument(null);
        updatedCaseDetails.getData().getCaseBundles().get(1).getValue().setStitchedDocument(null);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveBundles.aboutToStart(updatedCaseDetails);

        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems())
            .hasSize(3);
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems().getFirst().getLabel())
            .isEqualTo(caseData.getCaseBundles().getFirst().getValue().getDateAndTime() + " -- -cicBundle.pdf");
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems().get(1).getLabel())
            .isEqualTo(caseData.getCaseBundles().get(1).getValue().getDateAndTime() + " -- -cicBundle.pdf");
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems().get(2).getLabel())
            .isEqualTo(caseData.getCaseBundles().get(2).getValue().getDateAndTime() + " -- 3-cicBundle.pdf");
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getValue()).isNull();
    }

    @Test
    void shouldSuccessfullyDeleteBundlesOnAboutToSubmit() {
        UUID dynamicListElementCode1 = UUID.randomUUID();
        UUID dynamicListElementCode2 = UUID.randomUUID();

        List<DynamicListElement> removeBundleLabels = List.of(
            DynamicListElement.builder()
                .code(dynamicListElementCode1)
                .label(caseData.getCaseBundles().getFirst().getValue().getDateAndTime() + " -- 1-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(dynamicListElementCode2)
                .label(caseData.getCaseBundles().get(1).getValue().getDateAndTime() + " -- 2-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(UUID.randomUUID())
                .label(caseData.getCaseBundles().get(2).getValue().getDateAndTime() + " -- 3-cicBundle.pdf")
                .build()
        );

        List<DynamicListElement> selectedRemoveBundleLabels = List.of(
            DynamicListElement.builder()
                .code(dynamicListElementCode1)
                .label(caseData.getCaseBundles().getFirst().getValue().getDateAndTime() + " -- 1-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(dynamicListElementCode2)
                .label(caseData.getCaseBundles().get(1).getValue().getDateAndTime() + " -- 2-cicBundle.pdf")
                .build()
        );

        caseData.getCicCase().setRemoveBundlesList(DynamicMultiSelectList.builder()
            .listItems(removeBundleLabels)
            .value(selectedRemoveBundleLabels)
            .build());

        updatedCaseDetails.setData(caseData);

        String bundleUUID3 = caseData.getCaseBundles().get(2).getValue().getId();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveBundles.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        assertThat(response.getData().getCaseBundles()).hasSize(1);
        assertThat(response.getData().getCaseBundles().getFirst().getId()).isEqualTo("1");
        assertThat(response.getData().getCaseBundles().getFirst().getValue().getId()).isEqualTo(bundleUUID3);
        assertThat(response.getData().getCaseBundleIdsAndTimestamps()).hasSize(1);
        assertThat(response.getData().getCaseBundleIdsAndTimestamps().getFirst().getId()).isEqualTo("1");
        assertThat(response.getData().getCaseBundleIdsAndTimestamps().getFirst().getValue().getBundleId()).isEqualTo(bundleUUID3);
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems()).isNull();
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getValue()).isNull();
    }

    @Test
    void shouldSuccessfullySubmit() {
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        SubmittedCallbackResponse response = caseworkerRemoveBundles.submitted(updatedCaseDetails, beforeDetails);
        assertThat(response.getConfirmationHeader()).isEqualTo("# Case Updated");
    }

    @Test
    void shouldReturnErrorWhenNoBundlesSelectedForDeletion() {
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        UUID dynamicListElementCode1 = UUID.randomUUID();
        UUID dynamicListElementCode2 = UUID.randomUUID();

        List<DynamicListElement> removeBundleLabels = List.of(
            DynamicListElement.builder()
                .code(dynamicListElementCode1)
                .label(caseData.getCaseBundles().getFirst().getValue().getDateAndTime() + " -- 1-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(dynamicListElementCode2)
                .label(caseData.getCaseBundles().get(1).getValue().getDateAndTime() + " -- 2-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(UUID.randomUUID())
                .label(caseData.getCaseBundles().get(2).getValue().getDateAndTime() + " -- 3-cicBundle.pdf")
                .build()
        );

        caseData.getCicCase().setRemoveBundlesList(DynamicMultiSelectList.builder()
            .listItems(removeBundleLabels)
            .value(new ArrayList<>())
            .build());

        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            selectBundles.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).isEqualTo("Select at least one bundle to remove");

        caseData.getCicCase().setRemoveBundlesList(DynamicMultiSelectList.builder()
            .listItems(removeBundleLabels)
            .value(removeBundleLabels)
            .build());

        AboutToStartOrSubmitResponse<CaseData, State> successResponse =
            selectBundles.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(successResponse.getErrors()).isEmpty();
    }

}
