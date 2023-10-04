package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.Notification;
import uk.gov.hmcts.sptribs.caseworker.model.Notifications;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotifyProxyClient;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.exception.IdamNotificationException;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.services.CaseManagementService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_INFORMATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CLOSURE_REASON;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Component
@Slf4j
public class CaseWithdrawnNotification {
/*

    @Autowired
    private NotificationServiceCIC notificationService;
*/
    @Autowired
    CaseManagementService caseManagementService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private NotifyProxyClient notifyProxyClient;

    @Autowired
    private NotificationHelper notificationHelper;

    @Autowired
    private RestTemplate restTemplateNotify;

    @Value("${notify_proxy.url}")
    private String notificationUrl;

    @Value("/notifications/email")
    private String emailUrlPath;

    //@Override
    public NotificationRequest sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);
        NotificationRequest notificationRequest;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {

            notificationRequest = sendEmailNotification(cicCase.getEmail(), templateVars);
            //cicCase.setSubjectNotifyList(caseWithdrawnNotifyResponse);

            caseManagementService.updateNotificationDetails(authTokenGenerator.generate(),
                Long.parseLong(caseNumber.replace("-", "")), notificationRequest);
            return notificationRequest;
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars);
        }

        return null;
    }

    //@Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            /*NotificationResponse caseWithdrawnNotifyResponse =
                sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            cicCase.setRepNotificationResponse(caseWithdrawnNotifyResponse);*/
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    //@Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        /*NotificationResponse caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars);
        cicCase.setResNotificationResponse(caseWithdrawnNotifyResponse);*/
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            NotificationResponse caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getApplicantEmailAddress(), templateVars);
            cicCase.setAppNotificationResponse(caseWithdrawnNotifyResponse);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
    private NotificationRequest sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest notificationRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_WITHDRAWN_EMAIL);
        //return notificationService.sendEmail(notificationRequest);

        String serviceAuthToken = authTokenGenerator.generate();
        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(new StringBuilder(notificationUrl).append(emailUrlPath).toString());
        log.info("Notification URL in Refunds app email {}",builder.toUriString());
        try {
            ResponseEntity<String> responseEntity = restTemplateNotify.exchange(builder.toUriString(), HttpMethod.POST,
            new HttpEntity<>(notificationRequest, getHttpHeaders()),String.class);
        } catch (HttpClientErrorException exc) { // s2s returns 401 if token is invalid...
            throw new NotificationException(exc);
        }



        //ResponseEntity<?> responseEntity = notifyProxyClient.
        //    sendEmailNotification(authorisation, serviceAuthToken, getHttpHeaders(), notificationRequest);
        //String isNotificationSent = (String) responseEntity.getBody();
        return notificationRequest;
    }

    private MultiValueMap<String,String> getHttpHeaders() {
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        String serviceAuthToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY3BheV9idWJibGUiLCJleHAiOjE2OTYzNTY5NDF9.EVX6H5jOCVmqKPLCfMPeSWWc9D0quJtzq0r-5jC1T87l3nuYe8JVWMY--16-HKgUeZPS2D8kqcTHlLf-cAqxpg";
        String authorisation = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJiYXJwcmVwcm9kQG1haWxpbmF0b3IuY29tIiwiY3RzIjoiT0FVVEgyX1NUQVRFTEVTU19HUkFOVCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6ImJhOTllYjE5LWJiNGMtNDUxNi1iZmY5LTdjZjQwOGUzNGQwMi0xNzE5Mzk5NjUiLCJzdWJuYW1lIjoiYmFycHJlcHJvZEBtYWlsaW5hdG9yLmNvbSIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tYWF0Mi5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiQTVPeXowOUdPQzFUQTJTc2dLZmJmRzRZbzB3IiwiYXVkIjoicGF5YnViYmxlIiwibmJmIjoxNjk2MzQyNTM2LCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lIjoxNjk2MzQyNTM2LCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTY5NjM3MTMzNiwiaWF0IjoxNjk2MzQyNTM2LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiWk04SDJuZGpRV1JmOG9Ua2VCT2V5T0V6WDF3In0.nScIeOjXa-t2wjjj3e22jsROeYQokzuetZblf1ogVLLDD58zPHXL89FknNVbk-OhBIOwMIq_3JGgVBFPR98xpucvKigMdjVNo0ruBKgNr8sgLopEtsXGpXmFZWLb-T4QLYPtBju28CPxWTz0SXR2kSbMWkjvSNTvmzCE2Lhvn5H9NXvr0Qa5KAERKFrKJCZoD1VyuMYxwY8IWBn9M9Th_ReJ1k2IEuD3D9ZImOhfpbSFRzeIvhr0DSQ0REy2orFd1EkmLiGFm06lONM1VPlaMFD0YM485O61bGN76yK8SEiWQlpgXGGDzSXZhQFFFk8fEtT5Ss5CXLABBypFdMuzCw";

        inputHeaders.put("content-type", Arrays.asList("application/json"));
        inputHeaders.put("authorization", Arrays.asList(authorisation));
        inputHeaders.put("ServiceAuthorization", Arrays.asList(serviceAuthToken));
        log.info("HttpHeader {}", inputHeaders);
        return inputHeaders;
    }

    private String getServiceAuthorisationToken() {
        try {
            return authTokenGenerator.generate();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IdamNotificationException("S2S", e.getStatusCode(), e);
        } catch (Exception e) {
            throw new IdamNotificationException("S2S", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }

    private void sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.CASE_WITHDRAWN_POST);
        //notificationService.sendLetter(letterRequest);
    }

    private void addCaseClosedTemplateVars(CaseData caseData, Map<String, Object> templateVars) {
        CloseCase closeCase = caseData.getCloseCase();
        String additionalDetail = StringUtils.isNotEmpty(closeCase.getAdditionalDetail())
            ? closeCase.getAdditionalDetail() : NONE_PROVIDED;

        templateVars.put(CLOSURE_REASON, closeCase.getCloseCaseReason());
        templateVars.put(CLOSURE_INFORMATION, additionalDetail);
    }
}
