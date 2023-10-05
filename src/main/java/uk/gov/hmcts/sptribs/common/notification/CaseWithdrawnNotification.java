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
            return notificationRequest;
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
        return null;
    }

    //@Override
    public NotificationRequest sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);
        NotificationRequest notificationRequest;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            notificationRequest = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars);
            return notificationRequest;
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
        return null;
    }

    //@Override
    public NotificationRequest sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, respondentTemplateVars);

        NotificationRequest notificationRequest = sendEmailNotification(cicCase.getRespondentEmail(), respondentTemplateVars);
        return notificationRequest;
    }

    //@Override
    public NotificationRequest sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);
        addCaseClosedTemplateVars(caseData, templateVars);

        NotificationRequest notificationRequest;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            notificationRequest = sendEmailNotification(cicCase.getRespondentEmail(), templateVars);
            return notificationRequest;
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getApplicantAddress(), templateVars);
            sendLetterNotification(templateVars);
        }
        return null;
    }

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
        String serviceAuthToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY3BheV9idWJibGUiLCJleHAiOjE2OTY0MjgxNTR9.32cGi19I2ssq5NqjbYkjne65jH0_j-fugKol0VNh_x7qB0BlrU4o5OPUvOYUW96HqHDcEYcUcNsNxhMM5BY6pg";
        String authorisation = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJiYXJwcmVwcm9kQG1haWxpbmF0b3IuY29tIiwiY3RzIjoiT0FVVEgyX1NUQVRFTEVTU19HUkFOVCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6ImUxYzlhMDAzLTNmN2ItNDFjNy04OTM3LWU4OTdmZTU5ZjRiNy0xMTU0NjUyIiwic3VibmFtZSI6ImJhcnByZXByb2RAbWFpbGluYXRvci5jb20iLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWFhdDIuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6Im5qNTNaNjZSOFlSMnBiR0p4c3lqM3Zpcks3TSIsImF1ZCI6InBheWJ1YmJsZSIsIm5iZiI6MTY5NjQxMzc3MywiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIl0sImF1dGhfdGltZSI6MTY5NjQxMzc3MywicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE2OTY0NDI1NzMsImlhdCI6MTY5NjQxMzc3MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6Ik9iMm5SejlGR0d3dnYyY2pGUEF0Tk1TVUMyUSJ9.OErQN7-vomjcuonLAdmJzfGj-LDix-4y0yyrSLHLi0BYw2yXwUfDb6TnprUtLY08krYeZUspdqy-fiNzRDfVu1PF3BDuUOhvkWhRKdhYpj3XmAxVos0Nd5dupAQzOCsOVfpP_66zv5BLdJNQQvdOPnnWTH6o03XASb0wpgBNlmX94CIQuOmkH2m1rwxCxtil-n_NOdum4KcJ95rsnIpdZvycMLqvqN09QEeT30Hkg-h8fl0YaqgtXMZB6Ple93RDLMbvempum21TghjVBuFUTNWaAE1VjCvcrPtgmM7bjO0fwASGkrvK1HUd5PoElumhQGkxUdoemulkIqvjrv9ZYw";

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
