package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DynamicListUtilTest {

    @Test
    void shouldGetStatusForPrivateFalse() {

        //When
        DynamicList result = DynamicListUtil.createDynamicListWithOneElement("element");

        //Then
        assertThat(result).isNotNull();
    }

}
