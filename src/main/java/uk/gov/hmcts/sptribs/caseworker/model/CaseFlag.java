package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlag {
    @CCD(
        label = "Explain why you are creating this flag.\n"
            + "Do not include any sensitive information such as personal details.",
        regex = "^[a-zA-Z]{0,200}$",
        hint = "You can enter up to 200 characters")
    private String additionalDetail;


    @CCD(
        label = "Why is a stay being added to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagLevel flagLevel;


    @CCD(
        label = "Why is a stay being added to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagParty partyLevel;

}
