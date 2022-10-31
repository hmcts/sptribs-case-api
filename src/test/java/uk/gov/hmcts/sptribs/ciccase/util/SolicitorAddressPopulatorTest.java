package uk.gov.hmcts.sptribs.ciccase.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.solicitor.client.organisation.OrganisationContactInformation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SolicitorAddressPopulatorTest {

    @Test
    public void shouldPopulateEmptyAddress(){

        OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder().country(null).build();

        assertThat(SolicitorAddressPopulator.populateSolicitorAddress(organisationContactInformation), is(""));

    }

    @Test
    public void shouldPopulateFullAddress(){

        OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder()
            .addressLine1("100")
            .addressLine2("Charles Street")
            .addressLine3("Great Town")
            .townCity("LIECESTER")
            .country("Middlesex")
            .postCode("7E12 PFD")
            .country("UK")
            .build();

        assertThat(SolicitorAddressPopulator.populateSolicitorAddress(organisationContactInformation), is("100\nCharles Street\nGreat Town\nLIECESTER\n7E12 PFD\nUK"));

    }
}
