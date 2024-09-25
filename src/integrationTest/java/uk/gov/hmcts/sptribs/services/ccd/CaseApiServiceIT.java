package uk.gov.hmcts.sptribs.services.ccd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.controllers.model.DssCaseDataRequest;
import uk.gov.hmcts.sptribs.edgecase.event.Event;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_CREATE_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_SUBMIT_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_UPDATE_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CaseApiServiceIT {

    public static final String START_EVENT_TOKEN = "startEventToken";
    private static AppsConfig.AppsDetails appsDetails;
    private static CaseData caseData;
    private static User user;
    private static CaseDataContent caseDataContent;
    private static uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> apiCaseDetails;
    private static CaseDetails reformCaseDetails;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private IdamService idamService;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CaseApiService caseApiService;

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContentCaptor;

    @BeforeAll
    static void setUp() {
        createTestObjects();
    }

    @Test
    void shouldCreateCase() {
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent(CITIZEN_CIC_CREATE_CASE);
        appsDetails.setEventIds(eventsConfig);

        caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_CREATE_CASE)
                .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        reformCaseDetails = objectMapper.convertValue(apiCaseDetails, new TypeReference<>() {});

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_CREATE_CASE)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        when(coreCaseDataApi.startForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            CITIZEN_CIC_CREATE_CASE
        )).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        final CaseDetails actualCaseDetails = caseApiService.createCase(TEST_AUTHORIZATION_TOKEN,
            caseData,
            appsDetails);

        assertThat(actualCaseDetails).isEqualTo(reformCaseDetails);
        verify(coreCaseDataApi, times(1))
            .submitForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                true,
                caseDataContent
            );
        verify(coreCaseDataApi, times(1))
            .startForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                CITIZEN_CIC_CREATE_CASE
            );
    }

    @Test
    void shouldVerifyGetCaseDataContentForCreateCase() {
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent(CITIZEN_CIC_CREATE_CASE);
        appsDetails.setEventIds(eventsConfig);

        caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                    .id(CITIZEN_CIC_CREATE_CASE)
                    .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_CREATE_CASE)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            CITIZEN_CIC_CREATE_CASE
        )).thenReturn(startEventResponse);

        caseApiService.createCase(TEST_AUTHORIZATION_TOKEN,
            caseData,
            appsDetails);

        verify(coreCaseDataApi).submitForCitizen(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("2"),
            eq(ST_CIC_JURISDICTION),
            eq(ST_CIC_CASE_TYPE),
            eq(true),
            caseDataContentCaptor.capture()
        );

        final CaseDataContent capturedCaseDataContent = caseDataContentCaptor.getValue();

        assertThat(capturedCaseDataContent).isNotNull();
        assertThat(capturedCaseDataContent.getData()).isEqualTo(caseData);
        assertThat(capturedCaseDataContent.getEventToken()).isEqualTo(START_EVENT_TOKEN);
        assertThat(capturedCaseDataContent.getEvent().getId()).isEqualTo(CITIZEN_CIC_CREATE_CASE);
        assertThat(capturedCaseDataContent).isEqualTo(caseDataContent);
    }

    @Test
    void shouldUpdateCaseForUpdateEvent() {
        reformCaseDetails = objectMapper.convertValue(apiCaseDetails, new TypeReference<>() {});

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent(CITIZEN_CIC_UPDATE_CASE);
        appsDetails.setEventIds(eventsConfig);

        final DssCaseDataRequest dssCaseDataRequest = DssCaseDataRequest.convertDssCaseDataToRequest(caseData.getDssCaseData());
        caseDataContent = CaseDataContent.builder()
            .data(dssCaseDataRequest)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                    .id(CITIZEN_CIC_UPDATE_CASE)
                    .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_UPDATE_CASE)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            appsDetails.getEventIds().getUpdateEvent()
        )).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        final CaseDetails actualCaseDetails = caseApiService.updateCase(TEST_AUTHORIZATION_TOKEN,
            Event.UPDATE,
            TEST_CASE_ID,
            caseData,
            appsDetails);

        assertThat(actualCaseDetails).isEqualTo(reformCaseDetails);
        verify(coreCaseDataApi, times(1))
            .submitEventForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                String.valueOf(TEST_CASE_ID),
                true,
                caseDataContent
            );
        verify(coreCaseDataApi, times(1))
            .startEventForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                String.valueOf(TEST_CASE_ID),
                appsDetails.getEventIds().getUpdateEvent()
            );
    }

    @Test
    void shouldVerifyGetCaseDataContentForUpdateCaseEvent() {
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent(CITIZEN_CIC_UPDATE_CASE);
        appsDetails.setEventIds(eventsConfig);

        final DssCaseDataRequest dssCaseDataRequest = DssCaseDataRequest.convertDssCaseDataToRequest(caseData.getDssCaseData());
        caseDataContent = CaseDataContent.builder()
            .data(dssCaseDataRequest)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_UPDATE_CASE)
                .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_UPDATE_CASE)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            appsDetails.getEventIds().getUpdateEvent()
        )).thenReturn(startEventResponse);

        caseApiService.updateCase(TEST_AUTHORIZATION_TOKEN,
            Event.UPDATE,
            TEST_CASE_ID,
            caseData,
            appsDetails);

        verify(coreCaseDataApi).submitEventForCitizen(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("2"),
            eq(ST_CIC_JURISDICTION),
            eq(ST_CIC_CASE_TYPE),
            eq(String.valueOf(TEST_CASE_ID)),
            eq(true),
            caseDataContentCaptor.capture()
        );

        final CaseDataContent capturedCaseDataContent = caseDataContentCaptor.getValue();

        assertThat(capturedCaseDataContent).isNotNull();
        assertThat(capturedCaseDataContent.getData()).isEqualTo(dssCaseDataRequest);
        assertThat(capturedCaseDataContent.getEventToken()).isEqualTo(START_EVENT_TOKEN);
        assertThat(capturedCaseDataContent.getEvent().getId()).isEqualTo(CITIZEN_CIC_UPDATE_CASE);
        assertThat(capturedCaseDataContent).isEqualTo(caseDataContent);
    }

    @Test
    void shouldUpdateCaseForDssUpdateCaseEvent() {
        reformCaseDetails = objectMapper.convertValue(apiCaseDetails, new TypeReference<>() {});

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateCaseEvent(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        appsDetails.setEventIds(eventsConfig);

        final DssCaseDataRequest dssCaseDataRequest = DssCaseDataRequest.convertDssCaseDataToRequest(caseData.getDssCaseData());
        caseDataContent = CaseDataContent.builder()
                .data(dssCaseDataRequest)
                .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                    .id(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
                    .build()
                )
                .eventToken(START_EVENT_TOKEN)
                .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            appsDetails.getEventIds().getUpdateCaseEvent()
        )).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        final CaseDetails actualCaseDetails = caseApiService.updateCase(TEST_AUTHORIZATION_TOKEN,
            Event.UPDATE_CASE,
            TEST_CASE_ID,
            caseData,
            appsDetails);

        assertThat(actualCaseDetails).isEqualTo(reformCaseDetails);
        verify(coreCaseDataApi, times(1))
            .submitEventForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                String.valueOf(TEST_CASE_ID),
                true,
                caseDataContent
            );
        verify(coreCaseDataApi, times(1))
            .startEventForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                String.valueOf(TEST_CASE_ID),
                appsDetails.getEventIds().getUpdateCaseEvent()
            );
    }

    @Test
    void shouldVerifyGetCaseDataContentForDssUpdateCaseEvent() {
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateCaseEvent(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        appsDetails.setEventIds(eventsConfig);

        final DssCaseDataRequest dssCaseDataRequest = DssCaseDataRequest.convertDssCaseDataToRequest(caseData.getDssCaseData());
        caseDataContent = CaseDataContent.builder()
            .data(dssCaseDataRequest)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
                .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            appsDetails.getEventIds().getUpdateCaseEvent()
        )).thenReturn(startEventResponse);

        caseApiService.updateCase(TEST_AUTHORIZATION_TOKEN,
            Event.UPDATE_CASE,
            TEST_CASE_ID,
            caseData,
            appsDetails);

        verify(coreCaseDataApi).submitEventForCitizen(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("2"),
            eq(ST_CIC_JURISDICTION),
            eq(ST_CIC_CASE_TYPE),
            eq(String.valueOf(TEST_CASE_ID)),
            eq(true),
            caseDataContentCaptor.capture()
        );

        final CaseDataContent capturedCaseDataContent = caseDataContentCaptor.getValue();

        assertThat(capturedCaseDataContent).isNotNull();
        assertThat(capturedCaseDataContent.getData()).isEqualTo(dssCaseDataRequest);
        assertThat(capturedCaseDataContent.getEventToken()).isEqualTo(START_EVENT_TOKEN);
        assertThat(capturedCaseDataContent.getEvent().getId()).isEqualTo(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        assertThat(capturedCaseDataContent).isEqualTo(caseDataContent);
    }

    @Test
    void shouldUpdateCaseForSubmitEvent() {
        reformCaseDetails = objectMapper.convertValue(apiCaseDetails, new TypeReference<>() {});

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setSubmitEvent(CITIZEN_CIC_SUBMIT_CASE);
        appsDetails.setEventIds(eventsConfig);

        final DssCaseDataRequest dssCaseDataRequest = DssCaseDataRequest.convertDssCaseDataToRequest(caseData.getDssCaseData());
        caseDataContent = CaseDataContent.builder()
            .data(dssCaseDataRequest)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_SUBMIT_CASE)
                .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_SUBMIT_CASE)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            appsDetails.getEventIds().getSubmitEvent()
        )).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            true,
            caseDataContent
        )).thenReturn(reformCaseDetails);

        final CaseDetails actualCaseDetails = caseApiService.updateCase(TEST_AUTHORIZATION_TOKEN,
            Event.SUBMIT,
            TEST_CASE_ID,
            caseData,
            appsDetails);

        assertThat(actualCaseDetails).isEqualTo(reformCaseDetails);
        verify(coreCaseDataApi, times(1))
            .submitEventForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                String.valueOf(TEST_CASE_ID),
                true,
                caseDataContent
            );
        verify(coreCaseDataApi, times(1))
            .startEventForCitizen(
                TEST_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                user.getUserDetails().getId(),
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                String.valueOf(TEST_CASE_ID),
                appsDetails.getEventIds().getSubmitEvent()
            );
    }

    @Test
    void shouldVerifyGetCaseDataContentForUpdateCaseSubmitEvent() {
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setSubmitEvent(CITIZEN_CIC_SUBMIT_CASE);
        appsDetails.setEventIds(eventsConfig);

        final DssCaseDataRequest dssCaseDataRequest = DssCaseDataRequest.convertDssCaseDataToRequest(caseData.getDssCaseData());
        caseDataContent = CaseDataContent.builder()
            .data(dssCaseDataRequest)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder()
                .id(CITIZEN_CIC_SUBMIT_CASE)
                .build()
            )
            .eventToken(START_EVENT_TOKEN)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CITIZEN_CIC_SUBMIT_CASE)
            .token(START_EVENT_TOKEN)
            .caseDetails(reformCaseDetails)
            .build();
        when(coreCaseDataApi.startEventForCitizen(
            TEST_AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION,
            user.getUserDetails().getId(),
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            String.valueOf(TEST_CASE_ID),
            appsDetails.getEventIds().getSubmitEvent()
        )).thenReturn(startEventResponse);

        caseApiService.updateCase(TEST_AUTHORIZATION_TOKEN,
            Event.SUBMIT,
            TEST_CASE_ID,
            caseData,
            appsDetails);

        verify(coreCaseDataApi).submitEventForCitizen(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("2"),
            eq(ST_CIC_JURISDICTION),
            eq(ST_CIC_CASE_TYPE),
            eq(String.valueOf(TEST_CASE_ID)),
            eq(true),
            caseDataContentCaptor.capture()
        );

        final CaseDataContent capturedCaseDataContent = caseDataContentCaptor.getValue();

        assertThat(capturedCaseDataContent).isNotNull();
        assertThat(capturedCaseDataContent.getData()).isEqualTo(dssCaseDataRequest);
        assertThat(capturedCaseDataContent.getEventToken()).isEqualTo(START_EVENT_TOKEN);
        assertThat(capturedCaseDataContent.getEvent().getId()).isEqualTo(CITIZEN_CIC_SUBMIT_CASE);
        assertThat(capturedCaseDataContent).isEqualTo(caseDataContent);
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void shouldReturnEventToken(Event event) {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        String eventId = "";
        StartEventResponse startEventResponse;
        switch (event) {
            case UPDATE_CASE:
                eventsConfig.setUpdateCaseEvent(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
                appsDetails.setEventIds(eventsConfig);
                eventId = appsDetails.getEventIds().getUpdateCaseEvent();
                startEventResponse = StartEventResponse.builder()
                    .eventId(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
                    .token(START_EVENT_TOKEN)
                    .caseDetails(reformCaseDetails)
                    .build();
                when(coreCaseDataApi.startForCitizen(
                    TEST_AUTHORIZATION_TOKEN,
                    SERVICE_AUTHORIZATION,
                    user.getUserDetails().getId(),
                    ST_CIC_JURISDICTION,
                    ST_CIC_CASE_TYPE,
                    CITIZEN_DSS_UPDATE_CASE_SUBMISSION
                )).thenReturn(startEventResponse);
                break;
            case UPDATE:
                eventsConfig.setUpdateEvent(CITIZEN_CIC_UPDATE_CASE);
                appsDetails.setEventIds(eventsConfig);
                eventId = appsDetails.getEventIds().getUpdateEvent();
                startEventResponse = StartEventResponse.builder()
                    .eventId(CITIZEN_CIC_UPDATE_CASE)
                    .token(START_EVENT_TOKEN)
                    .caseDetails(reformCaseDetails)
                    .build();
                when(coreCaseDataApi.startForCitizen(
                    TEST_AUTHORIZATION_TOKEN,
                    SERVICE_AUTHORIZATION,
                    user.getUserDetails().getId(),
                    ST_CIC_JURISDICTION,
                    ST_CIC_CASE_TYPE,
                    CITIZEN_CIC_UPDATE_CASE
                )).thenReturn(startEventResponse);
                break;
            case SUBMIT:
                eventsConfig.setSubmitEvent(CITIZEN_CIC_SUBMIT_CASE);
                appsDetails.setEventIds(eventsConfig);
                eventId = appsDetails.getEventIds().getSubmitEvent();
                startEventResponse = StartEventResponse.builder()
                    .eventId(CITIZEN_CIC_SUBMIT_CASE)
                    .token(START_EVENT_TOKEN)
                    .caseDetails(reformCaseDetails)
                    .build();
                when(coreCaseDataApi.startForCitizen(
                    TEST_AUTHORIZATION_TOKEN,
                    SERVICE_AUTHORIZATION,
                    user.getUserDetails().getId(),
                    ST_CIC_JURISDICTION,
                    ST_CIC_CASE_TYPE,
                    CITIZEN_CIC_SUBMIT_CASE
                )).thenReturn(startEventResponse);
                break;
            default:
                break;
        }

        caseApiService.getEventToken(
            TEST_AUTHORIZATION_TOKEN,
            user.getUserDetails().getId(),
            eventId,
            appsDetails);

        verify(coreCaseDataApi).startForCitizen(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("2"),
            eq(ST_CIC_JURISDICTION),
            eq(ST_CIC_CASE_TYPE),
            eq(eventId));
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void shouldReturnEventTokenForUpdate(Event event) {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        String eventId = "";
        StartEventResponse startEventResponse;
        switch (event) {
            case UPDATE_CASE:
                eventsConfig.setUpdateCaseEvent(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
                appsDetails.setEventIds(eventsConfig);
                eventId = appsDetails.getEventIds().getUpdateCaseEvent();
                startEventResponse = StartEventResponse.builder()
                    .eventId(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
                    .token(START_EVENT_TOKEN)
                    .caseDetails(reformCaseDetails)
                    .build();
                when(coreCaseDataApi.startEventForCitizen(
                    TEST_AUTHORIZATION_TOKEN,
                    SERVICE_AUTHORIZATION,
                    user.getUserDetails().getId(),
                    ST_CIC_JURISDICTION,
                    ST_CIC_CASE_TYPE,
                    String.valueOf(TEST_CASE_ID),
                    CITIZEN_DSS_UPDATE_CASE_SUBMISSION
                )).thenReturn(startEventResponse);
                break;
            case UPDATE:
                eventsConfig.setUpdateEvent(CITIZEN_CIC_UPDATE_CASE);
                appsDetails.setEventIds(eventsConfig);
                eventId = appsDetails.getEventIds().getUpdateEvent();
                startEventResponse = StartEventResponse.builder()
                    .eventId(CITIZEN_CIC_UPDATE_CASE)
                    .token(START_EVENT_TOKEN)
                    .caseDetails(reformCaseDetails)
                    .build();
                when(coreCaseDataApi.startEventForCitizen(
                    TEST_AUTHORIZATION_TOKEN,
                    SERVICE_AUTHORIZATION,
                    user.getUserDetails().getId(),
                    ST_CIC_JURISDICTION,
                    ST_CIC_CASE_TYPE,
                    String.valueOf(TEST_CASE_ID),
                    CITIZEN_CIC_UPDATE_CASE
                )).thenReturn(startEventResponse);
                break;
            case SUBMIT:
                eventsConfig.setSubmitEvent(CITIZEN_CIC_SUBMIT_CASE);
                appsDetails.setEventIds(eventsConfig);
                eventId = appsDetails.getEventIds().getSubmitEvent();
                startEventResponse = StartEventResponse.builder()
                    .eventId(CITIZEN_CIC_SUBMIT_CASE)
                    .token(START_EVENT_TOKEN)
                    .caseDetails(reformCaseDetails)
                    .build();
                when(coreCaseDataApi.startEventForCitizen(
                    TEST_AUTHORIZATION_TOKEN,
                    SERVICE_AUTHORIZATION,
                    user.getUserDetails().getId(),
                    ST_CIC_JURISDICTION,
                    ST_CIC_CASE_TYPE,
                    String.valueOf(TEST_CASE_ID),
                    CITIZEN_CIC_SUBMIT_CASE
                )).thenReturn(startEventResponse);
                break;
            default:
                break;
        }

        caseApiService.getEventTokenForUpdate(
            TEST_AUTHORIZATION_TOKEN,
            user.getUserDetails().getId(),
            eventId,
            String.valueOf(TEST_CASE_ID),
            appsDetails);

        verify(coreCaseDataApi).startEventForCitizen(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("2"),
            eq(ST_CIC_JURISDICTION),
            eq(ST_CIC_CASE_TYPE),
            eq(String.valueOf(TEST_CASE_ID)),
            eq(eventId)
        );
    }

    @Test
    void shouldGetCaseDetails() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        caseApiService.getCaseDetails(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID);

        verify(coreCaseDataApi).getCase(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq(String.valueOf(TEST_CASE_ID))
        );

    }

    private static void createTestObjects() {
        final DssCaseData dssCaseData = DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("CIC")
            .build();

        caseData = CaseData.builder()
            .dssCaseData(dssCaseData)
            .build();

        user = new User(
            TEST_AUTHORIZATION_TOKEN,
            UserDetails.builder()
                .roles(List.of("citizen"))
                .id("2")
                .build()
        );

        apiCaseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        apiCaseDetails.setId(TEST_CASE_ID);
        apiCaseDetails.setData(caseData);

        appsDetails = new AppsConfig.AppsDetails();
        appsDetails.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        appsDetails.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        appsDetails.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
    }
}
