package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class DssUploadedDocument {

    @CCD(
        label = "Documents generated",
        typeOverride = FieldType.Collection,
        typeParameterOverride = "DssDocumentInfo",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private List<ListValue<DssDocumentInfo>> dssDocuments;

    @CCD(
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String dssAdditionalCaseInformation;

    @CCD(
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String dssCaseUpdatedBy;

}
