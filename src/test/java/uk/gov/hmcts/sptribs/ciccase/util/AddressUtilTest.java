package uk.gov.hmcts.sptribs.ciccase.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AddressUtilTest {

    private static final String ADDRESS_LINE1 = RandomStringUtils.random(20, true, false);
    private static final String ADDRESS_LINE2 = RandomStringUtils.random(10, true, false);
    public static final String TEST_OVERSEAS_EXCEPTION_MESSAGE =
        "Cannot assert whether address is overseas or not due to null address or blank/null country";

    @Test
    public void shouldReturnNullWhenAddressNotPresent() {
        assertNull(AddressUtil.getPostalAddress(null));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsLongerThan25Chars() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1(String.format("%s,%s", ADDRESS_LINE1, ADDRESS_LINE2))
            .postTown("town")
            .postCode("postcode")
            .build();

        //Then
        assertThat(AddressUtil.getPostalAddress(addressGlobalUK),
            is(String.format("%s\n%s\ntown\npostcode", ADDRESS_LINE1, ADDRESS_LINE2)));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsLongerThan25CharsAndLine2Present() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1(String.format("%s,%s", ADDRESS_LINE1, ADDRESS_LINE2))
            .addressLine2("line2")
            .postTown("town")
            .postCode("postcode")
            .build();

        //Then
        assertThat(AddressUtil.getPostalAddress(addressGlobalUK),
            is(String.format("%s\n%s,line2\ntown\npostcode", ADDRESS_LINE1, ADDRESS_LINE2)));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsLessThan25Chars() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1(ADDRESS_LINE1)
            .addressLine2(ADDRESS_LINE2)
            .postTown("town")
            .postCode("postcode")
            .build();

        //Then
        assertThat(AddressUtil.getPostalAddress(addressGlobalUK),
            is(String.format("%s\n%s\ntown\npostcode", ADDRESS_LINE1, ADDRESS_LINE2)));
    }

    @Test
    public void shouldReturnPostalAddressWhenAddressPresentAndLine1IsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .postTown("town")
            .postCode("postcode")
            .build();

        //Then
        assertThat(AddressUtil.getPostalAddress(addressGlobalUK), is("town\npostcode"));
    }

    @Test
    public void shouldReturnTrueIfCountryIsNonSanitisedUkAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("u.k.")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnTrueIfCountryIsNonSanitised2UkAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("U-K")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnTrueIfCountryIsSanitisedUkAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("UK")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnTrueIfCountryIsEnglandAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("England")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnTrueIfCountryIsWalesAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("Wales")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnTrueIfCountryIsUkAndPostcodeIsNotScottish() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("UK")
            .postCode("SW1A 1AA")
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnTrueIfCountryIsUnitedKingdomAndPostcodeIsNotScottish() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("United Kingdom")
            .postCode("W1J7NT")
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(true));
    }

    @Test
    public void shouldReturnFalseIfCountryIsUkAndPostcodeIsScottish() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("UK")
            .postCode("EH43 6BD")
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(false));
    }

    @Test
    public void shouldReturnFalseIfCountryIsUnitedKingdomAndPostcodeIsScottish() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("United Kingdom")
            .postCode("FK20HF")
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(false));
    }

    @Test
    public void shouldReturnFalseIfCountryIsFranceAndPostcodeIsNotNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("France")
            .postCode("75005")
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(false));
    }

    @Test
    public void shouldReturnFalseIfCountryIsScotlandAndPostcodeIsNotNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("Scotland")
            .postCode("AB24 1AW")
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(false));
    }

    @Test
    public void shouldReturnFalseIfCountryIsFranceAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("France")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(false));
    }

    @Test
    public void shouldReturnFalseIfCountryIsScotlandAndPostcodeIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country("Scotland")
            .postCode(null)
            .build();

        //Then
        assertThat(AddressUtil.isEnglandOrWales(addressGlobalUK), is(false));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfAddressIsNull() {
        assertThatThrownBy(() -> AddressUtil.isEnglandOrWales(null))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(TEST_OVERSEAS_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfCountryIsNull() {
        //When
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .country(null)
            .postCode("SW1A1BB")
            .build();

        //Then
        assertThatThrownBy(() -> AddressUtil.isEnglandOrWales(addressGlobalUK))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(TEST_OVERSEAS_EXCEPTION_MESSAGE);
    }
}
