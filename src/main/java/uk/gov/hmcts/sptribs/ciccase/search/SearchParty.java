package uk.gov.hmcts.sptribs.ciccase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchPartyField;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;

import static java.util.List.of;

@Component
public class SearchParty implements CCDConfig<CaseData, State, UserRole> {

    private static final List<SearchPartyField> SEARCH_PARTY_LIST = of(
        SearchPartyField.builder().searchPartyName("cicCaseApplicantFullName")
            .searchPartyEmailAddress("cicCaseApplicantEmailAddress")
            .searchPartyAddressLine1("cicCaseApplicantAddress.AddressLine1")
            .searchPartyPostCode("cicCaseApplicantAddress.PostCode")
            .searchPartyDOB("cicCaseApplicantDateOfBirth")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build(),
        SearchPartyField.builder().searchPartyName("cicCaseFullName")
            .searchPartyEmailAddress("cicCaseEmail")
            .searchPartyAddressLine1("cicCaseAddress.AddressLine1")
            .searchPartyPostCode("cicCaseAddress.PostCode")
            .searchPartyDOB("cicCaseDateOfBirth")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build(),
        SearchPartyField.builder().searchPartyName("cicCaseRepresentativeFullName")
            .searchPartyEmailAddress("cicCaseRepresentativeEmailAddress")
            .searchPartyAddressLine1("cicCaseRepresentativeAddress.AddressLine1")
            .searchPartyPostCode("cicCaseRepresentativeAddress.PostCode")
            .searchPartyDOB("cicCaseRepresentativeDateOfBirth")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build(),
        SearchPartyField.builder().searchPartyName("cicCaseRespondentName")
            .searchPartyEmailAddress("cicCaseRespondentEmail")
            .searchPartyAddressLine1("")
            .searchPartyPostCode("")
            .searchPartyDOB("")
            .searchPartyCollectionFieldName("")
            .searchPartyDOD("")
            .build()
    );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.searchParty()
            .fields(SEARCH_PARTY_LIST);
    }

}
