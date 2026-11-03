package uk.gov.hmcts.sptribs.ciccase.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseUpdate;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;

@ExtendWith(MockitoExtension.class)
class CicaCaseUpdateServiceTest {
    private static final String CASE_ID = "1234567890123456";
    private static final String AUTH = "Bearer citizen-token";
    private static final String S2S = "service-token";

    @Mock
    private CicaCaseService cicaCaseService;
    @Mock
    private CicaRoleService cicaRoleService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private CicaCaseUpdateService service;

    private final CicaCaseUpdate update = new CicaCaseUpdate("Additional information", List.of(
        new CicaCaseUpdate.UploadedDocument("http://documents/1", "http://documents/1/binary", "evidence.pdf", "Evidence")
    ));

    @BeforeEach
    void setUp() {
        // Token generation is not reached when case membership is denied.
        org.mockito.Mockito.lenient().when(authTokenGenerator.generate()).thenReturn(S2S);
    }

    @Test
    void shouldSubmitOnlyNewInformationAsCitizenAfterCheckingMembership() {
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenReturn(started("event-token"));

        service.updateCase(CASE_ID, AUTH, update);

        var content = ArgumentCaptor.forClass(CaseDataContent.class);
        var calls = inOrder(cicaCaseService, coreCaseDataApi);
        calls.verify(cicaCaseService).checkIfUserHasAccess(CASE_ID, AUTH);
        calls.verify(coreCaseDataApi).startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        calls.verify(coreCaseDataApi).createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), content.capture());
        assertThat(content.getValue().getEventToken()).isEqualTo("event-token");
        assertThat(content.getValue().getEvent().getId()).isEqualTo(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        assertThat(content.getValue().getData()).isInstanceOf(Map.class);
        Map<?, ?> data = (Map<?, ?>) content.getValue().getData();
        assertThat(data.size()).isEqualTo(3);
        assertThat(data.get("dssCaseDataAdditionalInformation")).isEqualTo(update.additionalInformation());
        assertThat(data.get("dssCaseDataCaseTypeOfApplication")).isEqualTo("CIC");
        List<?> documents = (List<?>) data.get("dssCaseDataOtherInfoDocuments");
        assertThat(documents).hasSize(1);
        ListValue<?> entry = (ListValue<?>) documents.getFirst();
        assertThat(entry.getId()).isNotBlank();
        CitizenCICDocument document = (CitizenCICDocument) entry.getValue();
        assertThat(document.getDocumentLink().getUrl()).isEqualTo("http://documents/1");
        assertThat(document.getDocumentLink().getBinaryUrl()).isEqualTo("http://documents/1/binary");
        assertThat(document.getDocumentLink().getFilename()).isEqualTo("evidence.pdf");
        assertThat(document.getComment()).isEqualTo("Evidence");
        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldEnsureCreatorRoleAndRetryWhenStartingEventReturns404() {
        FeignException notFound = failure(404);
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenThrow(notFound).thenReturn(started("retry-token"));

        service.updateCase(CASE_ID, AUTH, update);

        var calls = inOrder(cicaCaseService, coreCaseDataApi, cicaRoleService);
        calls.verify(cicaCaseService).checkIfUserHasAccess(CASE_ID, AUTH);
        calls.verify(coreCaseDataApi).startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        calls.verify(cicaRoleService).ensureCreatorRole(CASE_ID, AUTH);
        calls.verify(coreCaseDataApi).startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        calls.verify(coreCaseDataApi).createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), any());
    }

    @Test
    void shouldRestartWithFreshTokenWhenSubmittingEventReturns404() {
        FeignException notFound = failure(404);
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenReturn(started("first-token"), started("retry-token"));
        when(coreCaseDataApi.createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), any()))
            .thenThrow(notFound).thenReturn(null);

        service.updateCase(CASE_ID, AUTH, update);

        var content = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataApi, times(2)).createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), content.capture());
        assertThat(content.getAllValues()).extracting(CaseDataContent::getEventToken)
            .containsExactly("first-token", "retry-token");
        assertThat(content.getAllValues().getFirst().getData()).isEqualTo(content.getValue().getData());
        verify(cicaRoleService).ensureCreatorRole(CASE_ID, AUTH);
    }

    @Test
    void shouldStopAfterOneRecoveryAttempt() {
        FeignException failure = failure(404);
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(failure);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, AUTH, update)).isSameAs(failure);

        verify(coreCaseDataApi, times(2)).startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        verify(cicaRoleService).ensureCreatorRole(CASE_ID, AUTH);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 409, 422, 500, 503, -1})
    void shouldNotAssignRoleOrRetryOtherFailures(int status) {
        FeignException failure = failure(status);
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(failure);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, AUTH, update)).isSameAs(failure);

        verify(coreCaseDataApi).startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldNotContactCcdOrAssignRoleForUnauthorisedUser() {
        doThrow(new UnauthorisedCaseAccessException("Access denied"))
            .when(cicaCaseService).checkIfUserHasAccess(CASE_ID, AUTH);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, AUTH, update))
            .isInstanceOf(UnauthorisedCaseAccessException.class);

        verifyNoInteractions(coreCaseDataApi, cicaRoleService, authTokenGenerator);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 409, 422, 500, 503, -1})
    void shouldNotAssignRoleOrRetryOtherSubmissionFailures(int status) {
        FeignException failure = failure(status);
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenReturn(started("event-token"));
        when(coreCaseDataApi.createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), any())).thenThrow(failure);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, AUTH, update)).isSameAs(failure);

        verify(coreCaseDataApi).createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), any());
        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldNotRetryWhenRoleAssignmentFails() {
        FeignException notFound = failure(404);
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(notFound);
        FeignException roleFailure = mock(FeignException.class);
        doThrow(roleFailure).when(cicaRoleService).ensureCreatorRole(CASE_ID, AUTH);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, AUTH, update)).isSameAs(roleFailure);

        verify(coreCaseDataApi).startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
    }

    @Test
    void shouldAcceptDocumentsOnlyOrTextOnlyUpdates() {
        when(coreCaseDataApi.startEvent(AUTH, S2S, CASE_ID, CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenReturn(started("event-token"));

        service.updateCase(CASE_ID, AUTH, new CicaCaseUpdate(null, update.documents()));
        service.updateCase(CASE_ID, AUTH, new CicaCaseUpdate("Text only", List.of()));

        verify(coreCaseDataApi, times(2)).createEvent(eq(AUTH), eq(S2S), eq(CASE_ID), any());
    }

    private StartEventResponse started(String token) {
        return StartEventResponse.builder().token(token).build();
    }

    private FeignException failure(int status) {
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(status);
        return exception;
    }
}
