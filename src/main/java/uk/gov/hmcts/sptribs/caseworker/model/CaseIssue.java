package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.DynamicMultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseIssue {
    @CCD(
        label = "Choose the additional documentation that should be sent to the respondent",
        typeOverride = MultiSelectList,
        typeParameterOverride = "AdditionalDocument",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<AdditionalDocument> additionalDocument;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicationEvidence",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ApplicationEvidence> applicationEvidences;

    @CCD(
        label = "Choose the additional documentation that should be sent to the respondent",
        typeOverride = MultiSelectList,
        typeParameterOverride = "TribunalDocuments",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<TribunalDocuments> tribunalDocuments;

    @CCD(typeOverride = DynamicMultiSelectList,
        typeParameterOverride = "DynamicList",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    private DynamicMultiSelectList documentList;

}
