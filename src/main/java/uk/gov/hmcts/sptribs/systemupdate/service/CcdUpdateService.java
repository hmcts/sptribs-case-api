package uk.gov.hmcts.sptribs.systemupdate.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.sptribs.ciccase.CriminalInjuriesCompensation.CASE_TYPE;
import static uk.gov.hmcts.sptribs.ciccase.CriminalInjuriesCompensation.JURISDICTION;

@Service
@Slf4j
public class CcdUpdateService {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseDetailsUpdater caseDetailsUpdater;

    public void submitEvent(final CaseDetails caseDetails,
                            final String eventId,
                            final User user,
                            final String serviceAuth) {

        final String caseId = caseDetails.getId().toString();
        final String userId = user.getUserDetails().getId();
        final String authorization = user.getAuthToken();

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);

        try {
            startAndSubmitEventForCaseworkers(caseDetails, eventId, serviceAuth, caseId, userId, authorization);
        } catch (final FeignException e) {

            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            if (e.status() == CONFLICT.value()) {
                throw new CcdConflictException(message, e);
            }

            throw new CcdManagementException(e.status(), message, e);
        }
    }

    public void submitEvent(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                            final String eventId,
                            final User user,
                            final String serviceAuth) {

        submitEvent(caseDetailsConverter.convertToReformModelFromCaseDetails(caseDetails), eventId, user, serviceAuth);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void submitEventWithRetry(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                                     final String eventId,
                                     final User user,
                                     final String serviceAuth) {

        submitEvent(caseDetails, eventId, user, serviceAuth);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void submitEventWithRetry(final CaseDetails caseDetails,
                                     final String eventId,
                                     final User user,
                                     final String serviceAuth) {

        submitEvent(caseDetails, eventId, user, serviceAuth);
    }

    @Retryable(value = {CcdManagementException.class})
    public void submitEventWithRetry(final String caseId,
                                     final String eventId,
                                     final CaseTask caseTask,
                                     final User user,
                                     final String serviceAuth) {

        log.info("Submit event with retry for Case ID: {}, Event ID: {}", caseId, eventId);

        final String userId = user.getUserDetails().getId();
        final String authorization = user.getAuthToken();

        try {
            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                CASE_TYPE,
                caseId,
                eventId);

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetailsUpdater.updateCaseData(caseTask, startEventResponse).getData());

            coreCaseDataApi.submitEventForCaseWorker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                CASE_TYPE,
                caseId,
                true,
                caseDataContent);
        } catch (FeignException e) {
            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            throw new CcdManagementException(e.status(), message, e);
        }
    }

    private void startAndSubmitEventForCaseworkers(final CaseDetails caseDetails,
                                                   final String eventId,
                                                   final String serviceAuth,
                                                   final String caseId,
                                                   final String userId,
                                                   final String authorization) {

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventId);

        final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
            startEventResponse,
            DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
            DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
            caseDetails.getData());

        coreCaseDataApi.submitEventForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent);
    }
}
