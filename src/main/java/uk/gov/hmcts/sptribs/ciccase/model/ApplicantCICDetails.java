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
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class ApplicantCICDetails {


    @CCD(label = "Is represented by a solicitor?")
    private YesOrNo solicitorRepresented;
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
        label = "Date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;


    @CCD(
        label = "What is their contact preference?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType contactDetailsPrefrence;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String email;


}
