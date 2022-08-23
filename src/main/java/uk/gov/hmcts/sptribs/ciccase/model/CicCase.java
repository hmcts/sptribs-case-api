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
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

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
        label = "Comment",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String comment;
    @CCD(
        //label = "Add a Subject",
        typeOverride = MultiSelectList,
        typeParameterOverride = "SubjectCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private SubjectCIC subjectCIC;

    @CCD(
        //label = "Add an Applicant",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicantCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private ApplicantCIC applicantCIC;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "RepresentativeCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private RepresentativeCIC representativeCic;

    @CCD(
        label = "Full Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fullName;

    @CCD(label = "Address")
    private AddressGlobalUK address;
    @CCD(
        label = "Phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phoneNumber;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String email;


    @CCD(
        label = "Date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;


    @CCD(
        label = "What is their contact preference type?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType contactPreferenceType;


    @CCD(
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
        label = "Police authority management incident",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String policeAuthority;
    @CCD(
        label = "Has the tribunal form received in time?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo formReceivedInTime;
    @CCD(
        label = "Has the applicant explained why they missed the deadline?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo missedTheDeadLineCic;

    @CCD(
        label = "Have any claims linked to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private YesOrNo claimLinkedToCic;

    @CCD(
        label = "Are there any ongoing compensation claims linked to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private YesOrNo compensationClaimLinkCIC;
    private ApplicantCICDetails applicantCICDetails;
    private RepresentativeCICDetails representativeCICDetails;
    private ContactPreferencesDetailsCIC contactPreferencesDetailsCIC;
    private CaseDocumentsCIC caseDocumentsCIC;


}
