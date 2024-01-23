package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;


@Data
@NoArgsConstructor
@Builder
@ToString
@AllArgsConstructor
public class DssDocumentInfo {

    @CCD(
        label = "Document",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private Document document;

    @CCD(
        label = "Comment",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private String comment;
}
