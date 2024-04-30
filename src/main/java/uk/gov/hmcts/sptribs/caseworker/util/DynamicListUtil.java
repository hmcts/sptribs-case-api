package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DynamicListUtil {
    private DynamicListUtil() {

    }

    public static DynamicList createDynamicListWithOneElement(String hearingSummary) {
        List<String> hearingSummaryList = new ArrayList<>();
        hearingSummaryList.add(hearingSummary);
        List<DynamicListElement> dynamicListElements = hearingSummaryList
            .stream()
            .sorted()
            .map(summary -> DynamicListElement.builder().label(summary).code(UUID.randomUUID()).build())
            .toList();

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }

    public static DynamicList createDynamicList(List<String> hearingSummary) {
        List<DynamicListElement> dynamicListElements = hearingSummary
            .stream()
            .sorted()
            .map(hearing -> DynamicListElement.builder().label(hearing).code(UUID.randomUUID()).build())
            .toList();

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }
}
