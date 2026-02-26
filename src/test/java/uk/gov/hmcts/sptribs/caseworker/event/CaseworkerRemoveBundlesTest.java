package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
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

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerRemoveBundles.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(REMOVE_BUNDLES);
    }

    @Test
    void shouldSuccessfullyDeleteBundlesOnAboutToSubmit() {
        //Given
        String bundleUUID1 = UUID.randomUUID().toString();
        String bundleUUID2 = UUID.randomUUID().toString();
        String bundleUUID3 = UUID.randomUUID().toString();

        final LocalDateTime bundleTimestamp1 = LocalDateTime.now();
        final LocalDateTime bundleTimestamp2 = LocalDateTime.now().minusDays(1);
        final LocalDateTime bundleTimestamp3 = LocalDateTime.now().minusDays(2);

        List<ListValue<Bundle>> existingBundles = new ArrayList<>();
        existingBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder()
                .id(bundleUUID1)
                .dateAndTime(bundleTimestamp1)
                .fileName("1-cicBundle.pdf")
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("2")
            .value(Bundle.builder()
                .id(bundleUUID2)
                .dateAndTime(bundleTimestamp2)
                .fileName("2-cicBundle.pdf")
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("3")
            .value(Bundle.builder()
                .id(bundleUUID3)
                .dateAndTime(bundleTimestamp3)
                .fileName("3-cicBundle.pdf")
                .build())
            .build());

        final CaseData caseData = caseData();

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
        UUID dynamicListElementCode1 = UUID.randomUUID();
        UUID dynamicListElementCode2 = UUID.randomUUID();
        UUID dynamicListElementCode3 = UUID.randomUUID();

        List<DynamicListElement> removeBundleLabels = List.of(
            DynamicListElement.builder()
                .code(dynamicListElementCode1)
                .label(bundleTimestamp1 + " -- 1-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(dynamicListElementCode2)
                .label(bundleTimestamp2 + " -- 2-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(dynamicListElementCode3)
                .label(bundleTimestamp3 + " -- 3-cicBundle.pdf")
                .build()
        );

        List<DynamicListElement> selectedRemoveBundleLabels = List.of(
            DynamicListElement.builder()
                .code(dynamicListElementCode1)
                .label(bundleTimestamp1 + " -- 1-cicBundle.pdf")
                .build(),
            DynamicListElement.builder()
                .code(dynamicListElementCode2)
                .label(bundleTimestamp2 + " -- 2-cicBundle.pdf")
                .build()
        );

        caseData.getCicCase().setRemoveBundlesList(DynamicMultiSelectList.builder()
            .listItems(removeBundleLabels)
            .value(selectedRemoveBundleLabels)
            .build());

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveBundles.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        //Then
        assertThat(response.getData().getCaseBundles()).hasSize(1);
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getListItems()).isNull();
        assertThat(response.getData().getCicCase().getRemoveBundlesList().getValue()).isNull();
    }

}
