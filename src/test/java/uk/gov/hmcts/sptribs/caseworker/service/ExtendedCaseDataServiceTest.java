package uk.gov.hmcts.sptribs.caseworker.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.ExtendedCaseDetails;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
public class ExtendedCaseDataServiceTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private ExtendedCaseDataApi caseDataApi;

    @InjectMocks
    private ExtendedCaseDataService caseDataService;

    @Test
    public void shouldGetDataClassification() {
        // Given
        ExtendedCaseDetails caseDetails = ExtendedCaseDetails.builder()
            .id(123L)
            .dataClassification(Map.of("field1", "PUBLIC"))
            .build();
        User user = TestDataHelper.getUser();
        when(request.getHeader(AUTHORIZATION)).thenReturn("user");
        when(idamService.retrieveUser("user")).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(caseDataApi.getExtendedCaseDetails(any(), eq("token"), eq("123"))).thenReturn(caseDetails);

        // When
        Map<String, Object> dataClassification = caseDataService.getDataClassification("123");

        // Then
        assertThat(dataClassification).containsExactly(Map.entry("field1", "PUBLIC"));
    }
}
