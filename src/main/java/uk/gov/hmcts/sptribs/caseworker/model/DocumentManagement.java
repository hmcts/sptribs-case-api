package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentManagement {

    @CCD(
        label = "Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> caseworkerCICDocument;

    @CCD(
        label = "Select the document to amend its details",
        typeOverride = MultiSelectList,
        typeParameterOverride = "AdditionalDocument",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<AdditionalDocument> documentsToAmend;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicationEvidence",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ApplicationEvidence> applicationEvidences;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "TribunalDocuments",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<TribunalDocuments> tribunalDocuments;

    @CCD(
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<ListValue<CaseworkerCICDocument>> documentList;
}
