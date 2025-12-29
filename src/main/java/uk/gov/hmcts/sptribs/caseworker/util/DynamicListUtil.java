package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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

    public static <T extends Enum<T>> DynamicList createDynamicListFromEnumSet(EnumSet<T> availableOptions,
                                                                               Function<T, String> labelFetcher,
                                                                               T selectedOption) {
        List<DynamicListElement> listItems = availableOptions.stream()
            .map(enumItem -> DynamicListElement.builder()
                .code(UUID.nameUUIDFromBytes(enumItem.name().getBytes()))
                .label(labelFetcher.apply(enumItem))
                .build())
            .toList();

        DynamicListElement selectedElement = (selectedOption != null)
            ? DynamicListElement.builder()
                .code(UUID.nameUUIDFromBytes(selectedOption.name().getBytes()))
                .label(labelFetcher.apply(selectedOption))
                .build()
            : DynamicListElement.builder().build();

        return DynamicList.builder()
            .listItems(listItems)
            .value(selectedElement)
            .build();
    }

    public static <T extends Enum<T>> T getEnumFromUuid(UUID uuid, Class<T> enumType) {
        if (uuid == null) {
            return null;
        }

        for (T constant : enumType.getEnumConstants()) {
            UUID constantUuid = UUID.nameUUIDFromBytes(constant.name().getBytes());

            if (constantUuid.equals(uuid)) {
                return constant;
            }
        }

        throw new IllegalArgumentException("No Enum found for UUID: " + uuid);
    }
}
