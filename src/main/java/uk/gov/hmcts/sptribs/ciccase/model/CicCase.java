package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.NextState;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class CicCase {

    @CCD(
        label = "Case Category",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "CaseCategory"
    )
    private CaseCategory caseCategory;

    @CCD(
        label = "Case Subcategory",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "CaseSubcategory"
    )
    private CaseSubcategory caseSubcategory;

    @CCD(
        label = "Case Received Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate caseReceivedDate;

    @CCD(
        label = "Is Tribunal Application On Time?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isTribunalApplicationOnTime;

    @CCD(
        label = "Is Late Tribunal Application Reason Given?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isLateTribunalApplicationReasonGiven;

    @CCD(
        label = "Named Parties",
        typeOverride = MultiSelectList,
        typeParameterOverride = "PartiesCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<PartiesCIC> partiesCIC;

    @CCD(

        label = "Case information recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "SubjectCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<SubjectCIC> subjectCIC;

    @CCD(

        label = "Case information recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicantCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ApplicantCIC> applicantCIC;

    @CCD(

        label = "Case information recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "RepresentativeCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<RepresentativeCIC> representativeCIC;

    @CCD(
        label = "Respondant name ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String respondantName = "Appeals team";

    @CCD(
        label = "Respondant organisation ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String respondantOrganisation = "CICA";

    @CCD(
        label = "Respondant email  ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String respondantEmail = "appeals.team@cica.gov.uk";

    @CCD(
        label = "Subject Full Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fullName;

    @CCD(label = "Subject Address")
    private AddressGlobalUK address;

    @CCD(
        label = "Subject Phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phoneNumber;

    @CCD(
        label = "Subject Email address",
        typeOverride = Email
    )
    private String email;


    @CCD(
        label = "Subject Date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;


    @CCD(
        label = "What is subject contact preference type?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType contactPreferenceType;


    @CCD(
        label = "Scheme",
        typeOverride = FixedList,
        typeParameterOverride = "SchemeCic",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private SchemeCic schemeCic;

    @CCD(
        label = "CICA reference number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String cicaReferenceNumber;

    @CCD(
        label = "Applicant Full Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String applicantFullName;

    @CCD(label = "Applicant Address")
    private AddressGlobalUK applicantAddress;

    @CCD(
        label = "Applicant Phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String applicantPhoneNumber;

    @CCD(
        label = "Applicant Email address",
        typeOverride = Email
    )
    private String applicantEmailAddress;

    @CCD(
        label = "Applicant Date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicantDateOfBirth;

    @CCD(
        label = "What is applicant contact preference?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType applicantContactDetailsPreference;

    @CCD(
        label = "Representative Full Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeFullName;

    @CCD(
        label = "Organisation or business name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeOrgName;

    @CCD(
        label = "Representative's reference",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeReference;

    @CCD(label = "Representative Address")
    private AddressGlobalUK representativeAddress;

    @CCD(
        label = "Representative Contact number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String representativePhoneNumber;

    @CCD(
        label = "Representative Email address",
        typeOverride = Email
    )
    private String representativeEmailAddress;

    @CCD(
        label = "Representative Date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate representativeDateOfBirth;

    @CCD(
        label = "What is representative contact preference?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType representativeContactDetailsPreference;

    @CCD(
        label = "Is the representative legally qualified?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private YesOrNo isRepresentativeQualified;

    private YesOrNo representativeDetailsObjects;


    @CCD(
        label = "Police authority management incident",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String policeAuthority;
    @CCD(
        label = "Have the tribunal forms been received in time?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo formReceivedInTime;
    @CCD(
        label = "Has the applicant explained why they missed the deadline?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo missedTheDeadLineCic;

    @CCD(
        label = "Have any claims linked to this case been lodged with CICA? ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private YesOrNo claimLinkedToCic;

    @CCD(
        label = "Are there any ongoing compensation claims linked to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private YesOrNo compensationClaimLinkCIC;

    @CCD(
        label = "Case Number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String caseNumber;


    @CCD(
        label = "Is there a representative?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo isRepresentativePresent;
    private CaseDocumentsCIC caseDocumentsCIC;
    private YesOrNo selectedCheckBox;

    @CCD(
        label = "Next State",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NextState afterStayState;


}
