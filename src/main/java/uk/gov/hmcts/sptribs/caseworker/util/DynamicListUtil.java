package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }
}
