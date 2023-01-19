package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftOrderMainContentCIC {

    @CCD(
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContent;

    @CCD(
       // label = "Final decision notice preview",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document orderTemplateIssued;
}
