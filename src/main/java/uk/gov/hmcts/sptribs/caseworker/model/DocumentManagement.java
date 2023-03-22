package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentManagement {

    @CCD(
        label = "Upload a file to the system",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> caseworkerCICDocument;
}
