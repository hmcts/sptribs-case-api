package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.SendOrder;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;

import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.recordlisting.RecordListingConstants.HYPHEN;
@Service
@Slf4j
public class OrderService {

    public DynamicList getDueDates(List<ListValue<DateModel>> dueDates) {
      //    final var dueDatesFromSendOrder = sendOrder.getDueDates();
        return populateDueDates(dueDates);
    }

    private DynamicList populateDueDates(List<ListValue<DateModel>> dueDates) {

        List<DateModel> datesList = Objects.nonNull(dueDates)
            ? Arrays.asList(dueDates).stream().map(v -> v.get(0).getValue()).collect(Collectors.toList())
            : new ArrayList<>();

        List<DynamicListElement> duedatesList = dueDates
            .stream()
            .sorted()
            .map(venue -> DynamicListElement.builder().label(dueDates.toString()).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .value(DynamicListElement.builder().label("due dates").code(UUID.randomUUID()).build())
            .listItems(duedatesList)
            .build();
    }

}
