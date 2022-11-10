package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseDocumentsCIC;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOrder {

    @CCD(
        label = "How would you like to issue an order?"
    )
    private OrderIssuingType orderIssuingType;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CaseDocumentsCIC orderFile;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<DateModel>> dueDates;
}
