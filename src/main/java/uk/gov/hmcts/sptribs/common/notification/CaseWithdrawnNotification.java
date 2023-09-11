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
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

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
public class CaseWithdrawnNotification implements PartiesNotification {
/*

    @Autowired
    private NotificationServiceCIC notificationService;
*/
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

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {

            NotificationRequest notificationRequest = sendEmailNotification(cicCase.getEmail(), templateVars);
            //cicCase.setSubjectNotifyList(caseWithdrawnNotifyResponse);

            Notification notification = new Notification();
            notification.setReference(notificationRequest.getReference());
            notification.setTemplateId(notificationRequest.getTemplateId());
            notification.setCaseId(notificationRequest.getCaseId());
            Notifications notifications = new Notifications();
            notifications.setCaseWithdrawnNotification(notification);
            cicCase.setNotifications(notifications);

        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
    }

    @Override
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

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        /*NotificationResponse caseWithdrawnNotifyResponse = sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars);
        cicCase.setResNotificationResponse(caseWithdrawnNotifyResponse);*/
    }

    private NotificationRequest sendEmailNotification(final String destinationAddress, final Map<String, Object> templateVars) {
        NotificationRequest notificationRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.CASE_WITHDRAWN_EMAIL);
        //return notificationService.sendEmail(notificationRequest);

        String serviceAuthToken = authTokenGenerator.generate();
        serviceAuthToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY3BheV9idWJibGUiLCJleHAiOjE2OTQwODk1OTF9.yjfUdpnHdxYbt070M1Zg2qmjxfYKCHoU56g_iuH3HVw2gVoJCgq3GSlSX5bnzGSD2GctsMrWpM0ZnIG2bE4REg";
        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();
        authorisation = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJiYXJwcmVwcm9kQG1haWxpbmF0b3IuY29tIiwiY3RzIjoiT0FVVEgyX1NUQVRFTEVTU19HUkFOVCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjljZDZhN2U0LTc3NmQtNDJjNC1hOWU1LTM2YmVhODdkNTM3Ny0xMjk0NTQ3MzgiLCJzdWJuYW1lIjoiYmFycHJlcHJvZEBtYWlsaW5hdG9yLmNvbSIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tYWF0Mi5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiSmcxaWxFT3dLc0d2bTl1Nm5ST3N3SnZqbTBvIiwiYXVkIjoicGF5YnViYmxlIiwibmJmIjoxNjk0MDc1MTk0LCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lIjoxNjk0MDc1MTk0LCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTY5NDEwMzk5NCwiaWF0IjoxNjk0MDc1MTk0LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoidDNsOWZiSWwzYndFQV91YldqY2kzajBIaDFVIn0.qenInWJY5xNNS2xIiUMBdz6Y8aw6b-hjVveExHeEq-ozpues6PHz3yJcH_e0WPmNrEb3fwqzGo-L3RG9qiDN1QA0Px01HSdyPSQp7oobS88h6d5h19WS3uz3dq3WpIQCF5YbqNCvOEkzAKvRyAF1xAn6k3OaqAjS2YNyL1rQDu42ssnYxx5GxnNf40DccuE9ooswW556D8J5X-FEGB6NOtF3jtsJcvP_aZu7MrGzQiuMViTRK1XKuSrqBZIAY-2MNKwXbqb0F9iGsXi_6VT1ovweYLNk2AEJX7Xxa_ImGTkda3pu195HyTOWUkry8isUiYSkH67LqBgtLVEQuzmdCg";



        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(new StringBuilder(notificationUrl).append(emailUrlPath).toString());
        log.info("Notification URL in Refunds app email {}",builder.toUriString());
        ResponseEntity<String> responseEntity = restTemplateNotify.exchange(builder.toUriString(), HttpMethod.POST,
            new HttpEntity<>(notificationRequest, getHttpHeaders()),String.class);




        //ResponseEntity<?> responseEntity = notifyProxyClient.
        //    sendEmailNotification(authorisation, serviceAuthToken, getHttpHeaders(), notificationRequest);
        //String isNotificationSent = (String) responseEntity.getBody();
        return notificationRequest;
    }

    private MultiValueMap<String,String> getHttpHeaders() {
        MultiValueMap<String, String> inputHeaders = new LinkedMultiValueMap<>();
        String serviceAuthToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzcHRyaWJzX2Nhc2VfYXBpIiwiZXhwIjoxNjk0NDM0OTg1fQ.jwvIj74XbQ4S0UVHE7iQGB7ePdBM4IzZpsqp3mQ4PX4gZxQO-U_APsg5G79pkSlZJkJY_Ve5W32rf3Ode_g4gw";
        String authorisation = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJzdC10ZXN0NjZAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiNDcyMGE1M2YtZTQ2MS00Yzc3LTk5NmYtN2M2YThjZDc4ZGI4LTM5MDg0OTc5Iiwic3VibmFtZSI6InN0LXRlc3Q2NkBtYWlsaW5hdG9yLmNvbSIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tYWF0Mi5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiSlk0RllwTVRzS0xyZUZqeWZJazZDajZuZXhBIiwiYXVkIjoic3B0cmlicy1jYXNlLWFwaSIsIm5iZiI6MTY5NDQyMDU4MSwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIl0sImF1dGhfdGltZSI6MTY5NDQyMDU4MSwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE2OTQ0NDkzODEsImlhdCI6MTY5NDQyMDU4MSwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjNSS2k0OFZXVk5LXzZRV0tVelZhYWQ4LUU2TSJ9.lKPq6rIdH52g7AxOv9x6-uePOfswLGyJuRtoVQiDd2472FgZO6zfRrHwn93znKXPGNcZaAv4iTVKpDNw_TWzUmXkiASAzy_gGcR3GT1acOCBKfjvRC8jJTxC6SvcKRKpkKPbV69LXnnuV1MlaQmZoioATgNyZumvZeqxR2T1E-yGyHPKPO4STDlYM4bhSI2OLEiFrG8RKFdmGpbdhZdtHMdlhK-22DXDPuKG0e4mc7JC4Mu4qGV9MTUcOCW8Xx_iCLN5HcZzCzRUFJMJ9O282YsWThHNK8RATORljhuuyx-negYi4-zSMfpEBxJ_oqzFHe_Vx1Me4_gc22yLHD4FtA";

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
