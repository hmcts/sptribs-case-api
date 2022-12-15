package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;

@Service
@Slf4j
public class HearingService {
    public DynamicList getHearingDateDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        List<ListValue<HearingDate>> additionalHearingDateListValueList = data.getRecordListing().getAdditionalHearingDate();
        List<String> hearingDateList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(additionalHearingDateListValueList)) {
            for (ListValue<HearingDate> hearingDate : additionalHearingDateListValueList) {
                String date = hearingDate.getValue().getHearingVenueDate().toString()
                    + HYPHEN
                    + hearingDate.getValue().getHearingVenueTime();
                hearingDateList.add(date);
            }
            List<DynamicListElement> dynamicListElements = hearingDateList
                .stream()
                .sorted()
                .map(date -> DynamicListElement.builder().label(date).code(UUID.randomUUID()).build())
                .collect(Collectors.toList());

            return DynamicList
                .builder()
                .value(DynamicListElement.builder().label("date").code(UUID.randomUUID()).build())
                .listItems(dynamicListElements)
                .build();
        }
        return null;
    }
}
