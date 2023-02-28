package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.common.MappableObject;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DssCaseData implements MappableObject {

    @CCD(
        label = "caseTypeOfApplication",
        access = {CaseworkerWithCAAAccess.class}
    )
    private String caseTypeOfApplication;

    @CCD(
        label = "Subject Full Name",
        access = {DefaultAccess.class}
    )
    private String subjectFullName;

    @CCD(
        label = "Subject Date of Birth",
        access = {DefaultAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate subjectDateOfBirth;

    @CCD(
        label = "Subject Email Address",
        access = {DefaultAccess.class}
    )
    private String subjectEmailAddress;

    @CCD(
        label = "Subject Contact Number",
        access = {DefaultAccess.class}
    )
    private String subjectContactNumber;

    @CCD(
        label = "Subject Agree To Be Contacted",
        access = {DefaultAccess.class}
    )
    private YesOrNo subjectAgreeContact;

    @CCD(
        label = "Named Representative",
        access = {DefaultAccess.class}
    )
    private YesOrNo representation;

    @CCD(
        label = "Named Representative Qualified",
        access = {DefaultAccess.class}
    )
    private YesOrNo representationQualified;

    @CCD(
        label = "Representative Full Name",
        access = {DefaultAccess.class}
    )
    private String representativeFullName;

    @CCD(
        label = "Representative Organisation Name",
        access = {DefaultAccess.class}
    )
    private String representativeOrganisationName;

    @CCD(
        label = "Representative Contact Number",
        access = {DefaultAccess.class}
    )
    private String representativeContactNumber;

    @CCD(
        label = "Representative Email Address",
        access = {DefaultAccess.class}
    )
    private String representativeEmailAddress;

    @CCD(
        label = "Tribunal form uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "EdgeCaseDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<EdgeCaseDocument>> tribunalFormDocuments;

    @CCD(
        label = "Supporting uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "EdgeCaseDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<EdgeCaseDocument>> supportingDocuments;

    @CCD(
        label = "Other information uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "EdgeCaseDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<EdgeCaseDocument>> otherInfoDocuments;

}
