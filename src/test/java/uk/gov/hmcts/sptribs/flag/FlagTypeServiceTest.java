package uk.gov.hmcts.sptribs.flag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.service.AuthorisationService;
import uk.gov.hmcts.sptribs.flag.refdata.FlagTypeClient;
import uk.gov.hmcts.sptribs.flag.refdata.FlagTypeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class FlagTypeServiceTest {
    @InjectMocks
    private FlagTypeService flagTypeService;
    @Mock
    private FlagTypeClient client;

    @Mock
    private AuthorisationService authorisationService;

    @Test
    void shouldGetLinkReason() {
        //Given

        Object sample = new Object();
        when(authorisationService.getAuthorisation()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authorisationService.getServiceAuthorization()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(client.getFlags(any(), any())).thenReturn(sample);

        //When
        Object response = flagTypeService.getFlagTypes();

        //Then
        assertThat(response).isNotNull();
    }
}
