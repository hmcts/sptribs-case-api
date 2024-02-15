package uk.gov.hmcts.sptribs.judicialrefdata;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Judge;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ACCEPT_VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class JudicialServiceTest {

    @InjectMocks
    private JudicialService judicialService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private JudicialClient judicialClient;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    void shouldPopulateUserDynamicList() {
        //Given
        final UserProfileRefreshResponse userResponse1 = UserProfileRefreshResponse
            .builder()
            .fullName("John Smith")
            .personalCode("12345")
            .build();
        final UserProfileRefreshResponse userResponse2 = UserProfileRefreshResponse
            .builder()
            .fullName("John Doe")
            .personalCode("98765")
            .build();
        List<UserProfileRefreshResponse> responseEntity = List.of(userResponse1, userResponse2);
        final CaseData caseData = CaseData.builder().build();

        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(judicialClient.getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC"))).thenReturn(responseEntity);
        final DynamicList userList = judicialService.getAllUsers(caseData);

        //Then
        assertThat(userList).isNotNull();
        assertThat(caseData.getListing().getSummary().getJudgeList()).isNotNull();
        assertThat(caseData.getListing().getSummary().getJudgeList()).hasSize(2);

        verify(judicialClient).getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC")
        );
    }

    @Test
    void shouldReturnEmptyDynamicListWhenListFromJudicialRefDataCallIsNull() {
        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(judicialClient.getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC"))).thenReturn(null);
        final DynamicList regionList = judicialService.getAllUsers(caseData());

        //Then
        assertThat(regionList.getListItems()).isEmpty();
    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromJudicialRefDataCall() {
        //When
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        doThrow(FeignException.class)
            .when(judicialClient).getUserProfiles(
                TEST_SERVICE_AUTH_TOKEN,
                TEST_AUTHORIZATION_TOKEN,
                ACCEPT_VALUE,
                new JudicialUsersRequest("ST_CIC")
            );

        final DynamicList regionList = judicialService.getAllUsers(caseData());

        //Then
        assertThat(regionList.getListItems()).isEmpty();
        verify(judicialClient).getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC")
        );
    }

    @Test
    void shouldPopulateJudicialIdBasedOnDynamicListValue() {
        final UUID selectedJudgeUuid = UUID.randomUUID();
        final CaseData caseData = CaseData.builder()
            .listing(
                Listing.builder()
                    .summary(
                        HearingSummary.builder()
                            .judge(
                                DynamicList.builder()
                                    .value(
                                        DynamicListElement.builder()
                                            .label("mr judge")
                                            .code(selectedJudgeUuid)
                                            .build()
                                    )
                                    .build()
                            )
                            .judgeList(List.of(
                                ListValue.<Judge>builder()
                                    .value(
                                        Judge.builder()
                                            .uuid(UUID.randomUUID().toString())
                                            .build())
                                    .build(),
                                ListValue.<Judge>builder()
                                    .value(
                                        Judge.builder()
                                            .uuid(selectedJudgeUuid.toString())
                                            .personalCode("mr judges personal code")
                                            .build())
                                    .build(),
                                ListValue.<Judge>builder()
                                    .value(
                                        Judge.builder()
                                            .uuid(UUID.randomUUID().toString())
                                            .build())
                                    .build()
                            ))
                            .build()
                    )
                    .build()
            )
            .build();

        final String result = judicialService.populateJudicialId(caseData);

        assertThat(result).isEqualTo("mr judges personal code");
    }

    @Test
    void shouldPopulateJudicialIdAsEmptyStringWhenJudgeListDoesNotContainMatch() {
        final CaseData caseData = CaseData.builder()
            .listing(
                Listing.builder()
                    .summary(
                        HearingSummary.builder()
                            .judge(
                                DynamicList.builder()
                                    .value(
                                        DynamicListElement.builder()
                                            .label("mr judge")
                                            .code(UUID.randomUUID())
                                            .build()
                                    )
                                    .build()
                            )
                            .judgeList(List.of(
                                ListValue.<Judge>builder()
                                    .value(Judge.builder().uuid(UUID.randomUUID().toString()).build())
                                    .build(),
                                ListValue.<Judge>builder()
                                    .value(Judge.builder().uuid(UUID.randomUUID().toString()).build())
                                    .build(),
                                ListValue.<Judge>builder()
                                    .value(Judge.builder().uuid(UUID.randomUUID().toString()).build())
                                    .build()
                            ))
                            .build()
                    )
                    .build()
            )
            .build();

        final String result = judicialService.populateJudicialId(caseData);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldPopulateJudicialIdAsEmptyStringWhenJudgeListIsEmpty() {
        final CaseData caseData = CaseData.builder()
            .listing(
                Listing.builder()
                    .summary(
                        HearingSummary.builder()
                            .judge(
                                DynamicList.builder()
                                    .value(
                                        DynamicListElement.builder()
                                            .label("mr judge")
                                            .code(UUID.randomUUID())
                                            .build()
                                    )
                                    .build()
                            )
                            .judgeList(Collections.emptyList())
                            .build()
                    )
                    .build()
            )
            .build();

        final String result = judicialService.populateJudicialId(caseData);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldPopulateJudicialIdAsEmptyStringWhenJudgeIsNull() {
        final UUID selectedJudgeUuid = UUID.randomUUID();
        final CaseData caseData = CaseData.builder()
            .listing(
                Listing.builder()
                    .summary(
                        HearingSummary.builder()
                            .judgeList(List.of(
                                ListValue.<Judge>builder()
                                    .value(
                                        Judge.builder()
                                            .uuid(UUID.randomUUID().toString())
                                            .build())
                                    .build(),
                                ListValue.<Judge>builder()
                                    .value(
                                        Judge.builder()
                                            .uuid(selectedJudgeUuid.toString())
                                            .personalCode("mr judges personal code")
                                            .build())
                                    .build(),
                                ListValue.<Judge>builder()
                                    .value(
                                        Judge.builder()
                                            .uuid(UUID.randomUUID().toString())
                                            .build())
                                    .build()
                            ))
                            .build()
                    )
                    .build()
            )
            .build();

        final String result = judicialService.populateJudicialId(caseData);

        assertThat(result).isEqualTo("");
    }
}
