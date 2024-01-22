package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.TribunalCIC;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactParties {

    @CCD(
        label = "Contact parties recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "SubjectCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<SubjectCIC> subjectContactParties;

    @CCD(
        label = "Contact parties recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "RespondentCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<RespondentCIC> respondent;

    @CCD(
        label = "Contact parties recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "RepresentativeCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<RepresentativeCIC> representativeContactParties;

    @CCD(
        label = "Contact parties recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicantCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ApplicantCIC> applicantContactParties;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "TribunalCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<TribunalCIC> tribunal;

    @CCD(
        label = "Message",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String message;

}
