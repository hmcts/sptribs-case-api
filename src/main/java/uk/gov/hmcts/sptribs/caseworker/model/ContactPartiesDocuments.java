package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.DynamicMultiSelectList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactPartiesDocuments {

    @CCD(
        label = "Selected documents",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> previewDoc;

    @CCD(label = "Document 1", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument1;

    @CCD(label = "Document 2", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument2;

    @CCD(label = "Document 3", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument3;

    @CCD(label = "Document 4", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument4;

    @CCD(label = "Document 5", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument5;

    @CCD(label = "Document 6", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument6;

    @CCD(label = "Document 7", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument7;

    @CCD(label = "Document 8", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument8;

    @CCD(label = "Document 9", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument9;

    @CCD(label = "Document 10", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument reviewDocument10;

    @CCD(typeOverride = DynamicMultiSelectList,
        typeParameterOverride = "DynamicList",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    private DynamicMultiSelectList documentList;

    public void setReviewDocuments(List<ListValue<CaseworkerCICDocument>> selectedDocuments) {
        List<CaseworkerCICDocument> documents = selectedDocuments.stream()
            .map(ListValue::getValue)
            .limit(10)
            .toList();

        reviewDocument1 = documentAt(documents, 0);
        reviewDocument2 = documentAt(documents, 1);
        reviewDocument3 = documentAt(documents, 2);
        reviewDocument4 = documentAt(documents, 3);
        reviewDocument5 = documentAt(documents, 4);
        reviewDocument6 = documentAt(documents, 5);
        reviewDocument7 = documentAt(documents, 6);
        reviewDocument8 = documentAt(documents, 7);
        reviewDocument9 = documentAt(documents, 8);
        reviewDocument10 = documentAt(documents, 9);
    }

    private static CaseworkerCICDocument documentAt(List<CaseworkerCICDocument> documents, int index) {
        return index < documents.size() ? documents.get(index) : null;
    }

}
