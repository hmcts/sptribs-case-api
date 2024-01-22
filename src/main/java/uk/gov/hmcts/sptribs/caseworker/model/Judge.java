package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Judge {

    @CCD(
        label = "Judge UUID",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String uuid;

    @CCD(
        label = "Judge Full Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String judgeFullName;

    @CCD(
        label = "Judge Personal Code",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String personalCode;
}
