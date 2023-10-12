package uk.gov.hmcts.sptribs.caseworker.model;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parties {



    @CCD(
        label = "Party's title",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String partyTitle;

    @CCD(
        label = "Party's first name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
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
