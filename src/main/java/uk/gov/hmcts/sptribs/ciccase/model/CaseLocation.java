package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ComplexType(name = "CaseLocation", generate = false)
public class CaseLocation {

    @CCD(
        access = {DefaultAccess.class}
    )
    private String baseLocation;

    @CCD(
        access = {DefaultAccess.class}
    )
    private String region;
}
