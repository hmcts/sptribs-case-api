package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_EMAIL_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_EMAIL_NEW_CD;
import static uk.gov.hmcts.sptribs.notification.TemplateName.CONTACT_PARTIES_POST;

@Component
public class ContactPartiesNotification extends PartiesNotification {

    private static final int DOC_ATTACH_LIMIT = 10;

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    private record PartyFieldConfig(BiFunction<NotificationHelper, CaseData, Map<String, Object>> commonVars,
                                    Function<CicCase, String> emailAddress, Function<CicCase, AddressGlobalUK> postalAddress,
                                    Function<CicCase, ContactPreferenceType> preference,
                                    BiConsumer<CicCase, NotificationResponse> onEmailSent,
                                    BiConsumer<CicCase, NotificationResponse> onLetterSent) {
    }

    public ContactPartiesNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    public PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        return buildNotification(caseData,
            new PartyFieldConfig((
                (helper, cd) -> helper.getSubjectCommonVars(caseNumber, cd)),
                CicCase::getEmail,
                CicCase::getAddress,
                CicCase::getContactPreferenceType,
                CicCase::setSubjectNotifyList,
                CicCase::setSubjectLetterNotifyList));
    }

    @Override
    public PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        return buildNotification(caseData,
            new PartyFieldConfig(((helper, cd) -> helper.getApplicantCommonVars(caseNumber, cd)),
                CicCase::getApplicantEmailAddress,
                CicCase::getApplicantAddress,
                CicCase::getApplicantContactDetailsPreference,
                CicCase::setAppNotificationResponse,
                CicCase::setAppLetterNotificationResponse));
    }

    @Override
    public PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        return buildNotification(caseData,
            new PartyFieldConfig(((helper, cd) -> helper.getRepresentativeCommonVars(caseNumber, cd)),
                CicCase::getRepresentativeEmailAddress,
                CicCase::getRepresentativeAddress,
                CicCase::getRepresentativeContactDetailsPreference,
                CicCase::setRepNotificationResponse,
                CicCase::setRepLetterNotificationResponse));
    }

    @Override
    public PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsRespondent = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        templateVarsRespondent.put(CommonConstants.CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVarsRespondent.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments()
                                                                                               .getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments =
            DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData, caseData.getContactPartiesDocuments()
                                                                                   .getDocumentList());

        return new EmailNotification(cicCase.getRespondentEmail(),
            templateVarsRespondent,
            CONTACT_PARTIES_EMAIL,
            uploadedDocuments,
            selectedDocuments,
            saveToCicCase(CicCase::setResNotificationResponse));
    }

    @Override
    public PartyNotification buildTribunalNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVarsTribunal = notificationHelper().getTribunalCommonVars(caseNumber, caseData);
        templateVarsTribunal.put(CommonConstants.CIC_CASE_TRIBUNAL_NAME, TRIBUNAL_NAME_VALUE);
        templateVarsTribunal.put(CommonConstants.CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());

        Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments()
                                                                                               .getDocumentList(), DOC_ATTACH_LIMIT);
        List<CaseworkerCICDocument> selectedDocuments = DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData,
            caseData.getContactPartiesDocuments()
                    .getDocumentList());

        return new EmailNotification(TRIBUNAL_EMAIL_VALUE,
            templateVarsTribunal,
            CONTACT_PARTIES_EMAIL,
            uploadedDocuments,
            selectedDocuments,
            saveToCicCase(CicCase::setTribunalNotificationResponse));
    }

    private PartyNotification buildNotification(CaseData caseData, PartyFieldConfig fieldConfig) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = fieldConfig.commonVars()
                                                      .apply(notificationHelper(), caseData);
        templateVars.put(CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        templateVars.put(CONTACT_PARTY_INFO, cicCase.getNotifyPartyMessage());
        addDashboardLink(templateVars);

        ContactPreferenceType preference = fieldConfig.preference()
                                                      .apply(cicCase);
        if (preference == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = notificationHelper().buildDocumentList(caseData.getContactPartiesDocuments()
                                                                                                   .getDocumentList(), DOC_ATTACH_LIMIT);
            List<CaseworkerCICDocument> selectedDocuments = DocumentListUtil.getSelectedDocumentsFromDynamicList(caseData,
                caseData.getContactPartiesDocuments()
                        .getDocumentList());
            return new EmailNotification(fieldConfig.emailAddress()
                                                    .apply(cicCase),
                templateVars,
                getTemplateName(),
                uploadedDocuments,
                selectedDocuments,
                saveToCicCase(fieldConfig.onEmailSent()));
        } else {
            return new LetterNotification(fieldConfig.postalAddress()
                                                     .apply(cicCase),
                templateVars,
                CONTACT_PARTIES_POST,
                saveToCicCase(fieldConfig.onLetterSent()));
        }
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? CONTACT_PARTIES_EMAIL_NEW_CD : CONTACT_PARTIES_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
