package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.edgecase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.model.ContactPreference;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Applicant {

    @CCD(label = "First name")
    private String firstName;

    @CCD(label = "Last name")
    private String lastName;

    @CCD(
        label = "Date of Birth",
        access = {DefaultAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @CCD(
            label = "Contact Preference",
            access = {DefaultAccess.class}
    )
    private ContactPreference contactPreference;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String emailAddress;

    @CCD(label = "Applicant phoneNumber")
    private String phoneNumber;

    @CCD(label = "Applicant homePhoneNumber")
    private String homeNumber;

    @CCD(label = "Address1")
    private String address1;

    @CCD(label = "Address2")
    private String address2;

    @CCD(label = "Town")
    private String addressTown;

    @CCD(label = "Country")
    private String addressCountry;

    @CCD(label = "Post code")
    private String addressPostCode;

    @CCD(
            label = "Statement Of Truth",
            access = {DefaultAccess.class}
    )
    private YesOrNo statementOfTruth;

}
