package uk.gov.hmcts.sptribs.ciccase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

@Component
public class SearchParty implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.searchParty()
            .searchPartyName("cicCaseApplicantFullName")
            .searchPartyEmailAddress("cicCaseApplicantEmailAddress")
            .searchPartyAddressLine1("cicCaseApplicantAddress.AddressLine1")
            .searchPartyPostCode("cicCaseApplicantAddress.PostCode")
            .searchPartyDOB("cicCaseApplicantDateOfBirth")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build();

        configBuilder.searchParty()
            .searchPartyName("cicCaseFullName")
            .searchPartyEmailAddress("cicCaseEmail")
            .searchPartyAddressLine1("cicCaseAddress.AddressLine1")
            .searchPartyPostCode("cicCaseAddress.PostCode")
            .searchPartyDOB("cicCaseDateOfBirth")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build();

        configBuilder.searchParty()
            .searchPartyName("cicCaseRepresentativeFullName")
            .searchPartyEmailAddress("cicCaseRepresentativeEmailAddress")
            .searchPartyAddressLine1("cicCaseRepresentativeAddress.AddressLine1")
            .searchPartyPostCode("cicCaseRepresentativeAddress.PostCode")
            .searchPartyDOB("cicCaseRepresentativeDateOfBirth")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build();

        configBuilder.searchParty()
            .searchPartyName("cicCaseRespondentName")
            .searchPartyEmailAddress("cicCaseRespondentEmail")
            .searchPartyAddressLine1("")
            .searchPartyPostCode("")
            .searchPartyDOB("")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build();
    }

}
