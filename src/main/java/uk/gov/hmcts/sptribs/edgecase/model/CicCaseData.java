package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.document.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;
import uk.gov.hmcts.sptribs.edgecase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.edgecase.model.access.CollectionAccess;
import uk.gov.hmcts.sptribs.edgecase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CicCaseData {

    @JsonUnwrapped(prefix = "applicant")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private Applicant applicant = new Applicant();

    @CCD(
        label = "hyphenatedCaseReference",
        access = {CaseworkerAccess.class}
    )
    private String hyphenatedCaseRef;

    @CCD(
        label = "Due Date",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonUnwrapped()
    @Builder.Default
    private Application application = new Application();


    @CCD(
        label = "Documents generated",
        typeOverride = Collection,
        typeParameterOverride = "Document",
        access = {CollectionAccess.class}
    )
    private List<ListValue<EdgeCaseDocument>> documentsGenerated;

    @CCD(
        label = "Applicant uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "Document",
        access = {DefaultAccess.class}
    )
    private List<ListValue<EdgeCaseDocument>> applicantDocumentsUploaded;

    @CCD(
        label = "Documents uploaded",
        typeOverride = Collection,
        typeParameterOverride = "Document",
        access = {DefaultAccess.class}
    )
    private List<ListValue<EdgeCaseDocument>> documentsUploaded;

    @CCD(
        label = "Upload Document",
        access = {DefaultAccess.class}
    )
    private EdgeCaseDocument edgeCaseDocument;

    @CCD(
        label = "Applicant cannot upload supporting documents",
        access = {DefaultAccess.class}
    )
    private Set<DocumentType> applicantCannotUploadSupportingDocument;

    @CCD(
        label = "Applicant can not upload",
        access = {DefaultAccess.class}
    )
    private String applicantCannotUpload;
    @CCD(
        label = "Applicant can not upload",
        access = {DefaultAccess.class}
    )
    private YesOrNo findFamilyCourt;

    @CCD(
        label = "Family court name",
        access = {DefaultAccess.class}
    )
    private String familyCourtName;

    @CCD(
        label = "Family court email",
        access = {DefaultAccess.class}
    )
    private String familyCourtEmailId;

    @JsonIgnore
    public String formatCaseRef(long caseId) {
        String temp = String.format("%016d", caseId);
        return String.format(
            "%4s-%4s-%4s-%4s",
            temp.substring(0, 4),
            temp.substring(4, 8),
            temp.substring(8, 12),
            temp.substring(12, 16)
        );
    }

    @JsonIgnore
    public void addToDocumentsGenerated(final ListValue<EdgeCaseDocument> listValue) {

        final List<ListValue<EdgeCaseDocument>> documents = getDocumentsGenerated();

        if (isEmpty(documents)) {
            final List<ListValue<EdgeCaseDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsGenerated(documentList);
        } else {
            documents.add(0, listValue); // always add to start top of list
        }
    }

    public void sortUploadedDocuments(List<ListValue<EdgeCaseDocument>> previousDocuments) {
        if (isEmpty(previousDocuments)) {
            return;
        }

        Set<String> previousListValueIds = previousDocuments
            .stream()
            .map(ListValue::getId)
            .collect(Collectors.toCollection(HashSet::new));

        //Split the collection into two lists one without id's(newly added documents) and other
        // with id's(existing documents)
        Map<Boolean, List<ListValue<EdgeCaseDocument>>> documentsWithoutIds =
            this.getDocumentsUploaded()
                .stream()
                .collect(Collectors.groupingBy(listValue -> !previousListValueIds.contains(listValue.getId())));

        this.setDocumentsUploaded(sortDocuments(documentsWithoutIds));
    }

    private List<ListValue<EdgeCaseDocument>> sortDocuments(final Map<Boolean,
        List<ListValue<EdgeCaseDocument>>> documentsWithoutIds) {

        final List<ListValue<EdgeCaseDocument>> sortedDocuments = new ArrayList<>();

        final var newDocuments = documentsWithoutIds.get(true);
        final var previousDocuments = documentsWithoutIds.get(false);

        if (null != newDocuments) {
            sortedDocuments.addAll(0, newDocuments); // add new documents to start of the list
            sortedDocuments.addAll(1, previousDocuments);
            sortedDocuments.forEach(
                uploadedDocumentListValue -> uploadedDocumentListValue.setId(String.valueOf(UUID.randomUUID()))
            );
            return sortedDocuments;
        }

        return previousDocuments;
    }

    @JsonIgnore
    public void addToDocumentsUploaded(final ListValue<EdgeCaseDocument> listValue) {

        final List<ListValue<EdgeCaseDocument>> documents = getDocumentsUploaded();

        if (isEmpty(documents)) {
            final List<ListValue<EdgeCaseDocument>> documentList = new ArrayList<>();
            documentList.add(listValue);
            setDocumentsUploaded(documentList);
        } else {
            documents.add(0, listValue); // always add to start top of list
        }
    }

}
