package uk.gov.hmcts.sptribs.document.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@ToString
public class EdgeCaseDocument {

    @CCD(
        label = "Document",
        regex = ".doc,.docx,.pdf,.png,.xls,.xlsx,.jpg,.txt,.rtf,.rtf2,.gif,.mp3,.mp4"
    )
    private uk.gov.hmcts.ccd.sdk.type.Document documentLink;
}
