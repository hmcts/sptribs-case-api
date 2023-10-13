package uk.gov.hmcts.sptribs.caseworker.model;

import java.time.LocalDate;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchParty {



    @CCD(
        label = "Party's title",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonProperty("Party Title")
    private String partyTitle;

    @CCD(
        label = "Party's first name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonProperty("Party first name")
    private String partyFirstName;
    @CCD(
        label = "Party's last name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String partyLastName;

    @CCD(label = "Party's address")
    private AddressGlobalUK partyAddress;

    @CCD(
        label = "Party's email address",
        typeOverride = Email
    )
    private String apartyEmailAddress;

    @CCD(
        label = "Party's date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate partyDateOfBirth;

    @CCD(
        label = "Party's date of death",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate partyDateOfDeath;

}
