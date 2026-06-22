package uk.gov.hmcts.sptribs.statement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @CCD(
        label = "Statement from",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String party;

    @CCD(
        label = "Uploaded on",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String uploadedOn;

    @CCD(
        label = "Document",
        access = {DefaultAccess.class}
    )
    private Document document;
}
