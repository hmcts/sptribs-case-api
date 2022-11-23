package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@NoArgsConstructor
public class CaseIssue {
    @CCD(
        label = "Choose the additional documentation that should be sent to the respondent",
        typeOverride = MultiSelectList,
        typeParameterOverride = "AdditionalDocument",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<AdditionalDocument> additionalDocument;
}
