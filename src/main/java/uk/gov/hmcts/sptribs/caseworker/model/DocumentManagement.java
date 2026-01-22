package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CollectionDefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.AcknowledgementCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class DocumentManagement {

    @CCD(
        label = "Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {CollectionDefaultAccess.class}
    )
    @Builder.Default
    private List<ListValue<CaseworkerCICDocument>> caseworkerCICDocument = new ArrayList<>();

    @CCD(
        label = "Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocumentUpload",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    private List<ListValue<CaseworkerCICDocumentUpload>> caseworkerCICDocumentUpload = new ArrayList<>();

    @CCD(
        label = "Document",
        //typeParameterOverride = "CaseworkerCICDocumentUpload",
        access = {DefaultAccess.class}
    )
    @Builder.Default
    private AcknowledgementCICDocument acknowledgementCICDocument = new AcknowledgementCICDocument();


}
