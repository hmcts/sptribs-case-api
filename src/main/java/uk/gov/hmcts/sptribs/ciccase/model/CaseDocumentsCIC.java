package uk.gov.hmcts.sptribs.ciccase.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseDocumentsCIC {

    @CCD(
        label = "Add a file" + "\nUpload a file to the system",
        typeOverride = Collection,
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CICDocument>> applicantDocumentsUploaded;


    public static <T> List<ListValue<T>> addDocumentToTop(final List<ListValue<T>> documents, final T value) {
        return addDocumentToTop(documents, value, null);
    }

    public static <T> List<ListValue<T>> addDocumentToTop(final List<ListValue<T>> documents, final T value, final String id) {
        final var listItemId = isBlank(id) ? String.valueOf(randomUUID()) : id;
        final var listValue = new ListValue<T>(listItemId, value);
        final List<ListValue<T>> list = isEmpty(documents) ? new ArrayList<>() : new ArrayList<>(documents);

        list.add(0, listValue);

        return list;
    }



    public static <T> List<ListValue<T>> sortByNewest(final List<ListValue<T>> previous, final List<ListValue<T>> updated) {
        if (isEmpty(previous)) {
            return updated;
        }

        final var previousListValueIds = previous
            .stream()
            .map(ListValue::getId)
            .collect(toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other with id's(existing documents)
        final var documentsWithoutIds =
            updated
                .stream()
                .collect(groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        return sortDocuments(documentsWithoutIds);
    }

    public static <T> List<ListValue<T>> sortDocuments(final Map<Boolean, List<ListValue<T>>> documentsWithoutIds) {
        final List<ListValue<T>> sortedDocuments = new ArrayList<>();
        final var newDocuments = documentsWithoutIds.get(true);


        if (null != newDocuments) {
            sortedDocuments.addAll(0, newDocuments); // add new documents to start of the list
            sortedDocuments.forEach(
                uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(randomUUID()))
            );
            return sortedDocuments;
        }

        return documentsWithoutIds.getOrDefault(false, new ArrayList<>());
    }

    public static <T> boolean hasAddedDocuments(final List<ListValue<T>> after,
                                                final List<ListValue<T>> before) {

        if (isNull(before) && !after.isEmpty()) {
            return true;
        } else if (isNull(before) || isNull(after)) {
            return false;
        }

        return !after.stream()
            .allMatch(afterValue -> before.stream()
                .anyMatch(beforeValue -> Objects.equals(beforeValue.getId(), afterValue.getId())));
    }
}
