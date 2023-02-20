package uk.gov.hmcts.sptribs.ciccase.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseDocumentsCIC {

    @CCD(
        label = "Upload a file to the system",
        typeOverride = Collection,
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CICDocument>> applicantDocumentsUploaded;

}
