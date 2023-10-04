package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConcessionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConsentOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseRejectionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseRule27;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseStrikeOutDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseWarning;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseWithdrawalDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.Notification;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.common.notification.CaseWithdrawnNotification;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.services.ccd.CaseApiService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToCaseworkerDocument;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateUploadedDocuments;

@Component
@Slf4j
public class CaseworkerCloseTheCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration closeCaseWarning = new CloseCaseWarning();
    private static final CcdPageConfiguration closeCaseReasonSelect = new CloseCaseReasonSelect();
    private static final CcdPageConfiguration closeCaseWithdrawalDetails = new CloseCaseWithdrawalDetails();
    private static final CcdPageConfiguration closeCaseRejectionDetails = new CloseCaseRejectionDetails();
    private static final CcdPageConfiguration closeCaseConcessionDetails = new CloseCaseConcessionDetails();
    private static final CcdPageConfiguration closeCaseStrikeOutDetails = new CloseCaseStrikeOutDetails();
    private static final CcdPageConfiguration closeCaseConsentOrder = new CloseCaseConsentOrder();
    private static final CcdPageConfiguration closeCaseRule27 = new CloseCaseRule27();
    private static final CcdPageConfiguration closeCaseSelectRecipients = new CloseCaseSelectRecipients();

    @Autowired
    private JudicialService judicialService;

    @Autowired
    private CaseWithdrawnNotification caseWithdrawnNotification;

    @Autowired
    CaseApiService caseApiService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    AppsConfig appsConfig;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        var pageBuilder = closeCase(configBuilder);
        closeCaseWarning.addTo(pageBuilder);
        closeCaseReasonSelect.addTo(pageBuilder);
        closeCaseWithdrawalDetails.addTo(pageBuilder);
        closeCaseRejectionDetails.addTo(pageBuilder);
        closeCaseConcessionDetails.addTo(pageBuilder);
        closeCaseStrikeOutDetails.addTo(pageBuilder);
        closeCaseConsentOrder.addTo(pageBuilder);
        closeCaseRule27.addTo(pageBuilder);
        uploadDocuments(pageBuilder);
        closeCaseSelectRecipients.addTo(pageBuilder);
    }

    public PageBuilder closeCase(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CLOSE_THE_CASE)
            .forStates(CaseManagement)
            .name("Case: Close case")
            .showSummary()
            .description("Close the case")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::closed)
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_CLOSE_THE_CASE);

        DynamicList judicialUsersDynamicList = judicialService.getAllUsers();
        caseData.getCloseCase().setRejectionName(judicialUsersDynamicList);
        caseData.getCloseCase().setStrikeOutName(judicialUsersDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker close the case callback invoked for Case Id: {}", details.getId());
        var caseData = details.getData();
        updateCategoryToCaseworkerDocument(caseData.getCloseCase().getDocuments());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .build();
    }

    public SubmittedCallbackResponse closed(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {

        String message = MessageUtil.generateSimpleMessage(details.getData().getCicCase(), "Case closed",
            "Use 'Reinstate case' if this case needs to be reopened in the future.");
        try {
            sendCaseWithdrawnNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Case close notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case close notification failed %n## Please resend the notification"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(message)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        List<ListValue<CaseworkerCICDocument>> uploadedDocuments = data.getCloseCase().getDocuments();
        final List<String> errors = validateUploadedDocuments(uploadedDocuments);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private void sendCaseWithdrawnNotification(String caseNumber, CaseData caseData) {
        CicCase cicCase = caseData.getCicCase();
        NotificationRequest notificationRequest = new NotificationRequest();
        List<ListValue<Notification>> listValues = new ArrayList<>();

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            notificationRequest = caseWithdrawnNotification.sendToSubject(caseData, caseNumber);
            listValues = prepareNotificationList(notificationRequest, listValues);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            caseWithdrawnNotification.sendToRespondent(caseData, caseNumber);
            listValues = prepareNotificationList(notificationRequest, listValues);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            caseWithdrawnNotification.sendToRepresentative(caseData, caseNumber);
            listValues = prepareNotificationList(notificationRequest, listValues);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            caseWithdrawnNotification.sendToApplicant(caseData, caseNumber);
        }
/*

        listValues = prepareNotificationList(notificationRequest, listValues);

        caseData.getCicCase().setNotificationList(listValues);
        caseData.setCurrentEvent("updateNotification-1");

        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();

        String caseTypeOfApplication = "CIC";
        authorisation = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJzdC10ZXN0NjZAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiOWNkNmE3ZTQtNzc2ZC00MmM0LWE5ZTUtMzZiZWE4N2Q1Mzc3LTE3MDk2ODk4MyIsInN1Ym5hbWUiOiJzdC10ZXN0NjZAbWFpbGluYXRvci5jb20iLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWFhdDIuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IkhQM084R2lKUWlIVlQ0TmFCekEyUnF4TXFkdyIsImF1ZCI6InNwdHJpYnMtY2FzZS1hcGkiLCJuYmYiOjE2OTYzNDQ3NzQsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE2OTYzNDQ3NzQsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjk2MzczNTc0LCJpYXQiOjE2OTYzNDQ3NzQsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJhVklncDlwQlY2S3FzRUZxOVczejhPQUI0WWsifQ.szW63HiTfSIl9MelD0yUcOh21C0FO9Rf6fsnTuPzL_9KCHCtJjP7bRj8E8nNCAE9s3Sn9oDQ0daQUqHxQJsXL5sqis34K0DLvrZhuEqxpOjoeBeUPJP16PUMVybdlyKhWc-2O9J0YrMGSEQHoxrKDs7m4xghaaRWREdxi1plxvzkaF--fErtcozAmcQGBecUsxrQ20S93lK14az5m5iHpgAMiZzW1_unkSZ3fjgaJL9BW1NF-hzvxVjOZlCnGc79v1QAYwtmu_8cCwvJ180JsXu0-QUDThIPdKoVdPFtADLBDFdyzuW1uL8akwW3PmJt1cBuB0rJcQNB7O0a6tw7zw";
        caseApiService.updateCaseForCaseworker(authorisation, Long.parseLong(caseNumber.replace("-", "")), caseData,
                        AppsUtil.getExactAppsDetails(appsConfig, caseTypeOfApplication));
*/


    }

    private List<ListValue<Notification>> prepareNotificationList(NotificationRequest notificationRequest, List<ListValue<Notification>> listValues) {
        Notification notification = new Notification();
        notification.setReference(notificationRequest.getReference());
        notification.setTemplateId(notificationRequest.getTemplateId());
        notification.setCaseId(notificationRequest.getCaseId());


        var listValue = ListValue
            .<Notification>builder()
            .id("3")
            .value(notification)
            .build();

        listValues.add(listValue);
        return listValues;
    }

    private void uploadDocuments(PageBuilder pageBuilder) {
        String pageNameUpload = "closeCaseUploadDocuments";
        pageBuilder.page(pageNameUpload, this::midEvent)
            .pageLabel("Upload case documents")
            .label("LabelCloseCaseUploadDoc",
                """
                    Please upload copies of any information or evidence that you want to add to this case.
                    <h3>Files should be:</h3>
                    uploaded separately and not in one large file
                    a maximum of 100MB in size (larger files must be split)
                    labelled clearly, e.g. applicant-name-decision-notice.pdf\n\n\n\n
                    Note: If the remove button is disabled, please refresh the page to remove attachments
                    """)


            .complex(CaseData::getCloseCase)
            .optionalWithLabel(CloseCase::getDocuments, "File Attachments")
            .done();
    }
}
