package uk.gov.hmcts.sptribs.document.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@Data
@NoArgsConstructor
@ToString
public class EdgeCaseDocument {

    @CCD(
        label = "Document",
        regex = ".csv,.doc,.docx,.pdf,.png,.xls,.xlsx,.jpg,.txt,.rtf,.rtf2,.gif,.mp3,.mp4,.msg,.eml",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private uk.gov.hmcts.ccd.sdk.type.Document documentLink;

    @CCD(
        label = "Document description",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String comment;
}
