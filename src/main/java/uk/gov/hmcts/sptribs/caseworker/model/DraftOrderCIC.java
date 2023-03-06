package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftOrderCIC {

    @CCD(
        label = "Order File",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        categoryID = "A"
    )
    private Document templateGeneratedDocument;

    @JsonUnwrapped(prefix = "orderContent")
    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DraftOrderContentCIC draftOrderContentCIC = new DraftOrderContentCIC();


}
