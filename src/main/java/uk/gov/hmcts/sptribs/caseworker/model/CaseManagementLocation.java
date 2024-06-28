package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.GlobalSearchAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseManagementLocation {

    @CCD(
        label = "Base Location",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class, GlobalSearchAccess.class}
    )
    private String baseLocation;

    @CCD(
        label = "Region",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class, GlobalSearchAccess.class}
    )
    private String region;

}
