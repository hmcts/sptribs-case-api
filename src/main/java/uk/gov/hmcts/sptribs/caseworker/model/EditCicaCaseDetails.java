package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class EditCicaCaseDetails {

    @CCD(
        label = "CICA reference number",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String cicaReferenceNumber;

    @CCD(
        label = "CICA case worker",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String cicaCaseWorker;

    @CCD(
        label = "CICA case presenting officer",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String cicaCasePresentingOfficer;

}
