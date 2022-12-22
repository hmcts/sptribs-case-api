package uk.gov.hmcts.sptribs.caseworker.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class RecordListHelperTest {

    @InjectMocks
    private RecordListHelper recordListHelper;

    @Mock
    private LocationService locationService;

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();

        //When
        when(locationService.getAllRegions()).thenReturn(getMockedRegionData());
        recordListHelper.regionData(caseData);

        //Then
        assertThat(caseData.getRecordListing().getRegionList()).isNotNull();
        assertThat(caseData.getRecordListing().getRegionList().getListItems()).hasSize(1);

    }


    private DynamicList getMockedRegionData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("1-region")
            .code(UUID.randomUUID())
            .build();

        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }


}
