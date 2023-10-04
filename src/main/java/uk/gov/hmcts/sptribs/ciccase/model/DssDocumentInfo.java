package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;


@Data
@NoArgsConstructor
@Builder
@ToString
@AllArgsConstructor
public class DssDocumentInfo {

    @CCD(
        label = "Document"
    )
    private Document document;

    @CCD(
        label = "Comment"
    )
    private String comment;
}
