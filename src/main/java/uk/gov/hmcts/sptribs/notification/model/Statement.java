package uk.gov.hmcts.sptribs.notification.model;

import lombok.*;
import uk.gov.hmcts.ccd.sdk.api.*;
import uk.gov.hmcts.ccd.sdk.type.*;
import uk.gov.hmcts.sptribs.ciccase.model.access.*;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Statement {

    @CCD(
        label = "Document Saved Date",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String savedOn;

    @CCD(
        label = "party",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String party;

    @CCD(
        label = "Document url",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document documentUrl;

}
