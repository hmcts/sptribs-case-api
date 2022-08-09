package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Notifications {
    @CCD(
        label = "Is Named Party Subject?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isNamedPartySubject;

    @CCD(
        label = "Is Named Party Applicant?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isNamedPartyApplicant;

    @CCD(
        label = "Is Named Party Subject Rep?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isNamedPartySubjectRep;
}
