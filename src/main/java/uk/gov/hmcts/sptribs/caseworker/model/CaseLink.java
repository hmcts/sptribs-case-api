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

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseLink {
    @CCD(
        label = "Case Reference",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String caseReference;

    @CCD(
        label = "Reason for Link",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<LinkCaseReason> reasonForLink;

    @CCD(
        label = "createdDateTime",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDate createdDateTime;

    @CCD(
        label = "caseType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String caseType;
}
