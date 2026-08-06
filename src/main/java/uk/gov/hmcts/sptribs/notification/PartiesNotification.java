package uk.gov.hmcts.sptribs.notification;

import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class PartiesNotification {
    private final NotificationServiceCIC notificationService;
    private final NotificationHelper notificationHelper;

    protected PartiesNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    protected NotificationServiceCIC notificationService() {
        return notificationService;
    }

    protected NotificationHelper notificationHelper() {
        return notificationHelper;
    }

    public sealed interface PartyNotification permits EmailNotification, LetterNotification {
    }

    public record EmailNotification(String emailAddress, Map<String, Object> templateVars, TemplateName templateName,
                                    Map<String, String> documentTemplateVars, List<CaseworkerCICDocument> attachedDocuments,
                                    BiConsumer<CaseData, NotificationResponse> onSent) implements PartyNotification {
    }

    public record LetterNotification(AddressGlobalUK postalAddress, Map<String, Object> templateVars, TemplateName templateName,
                                     BiConsumer<CaseData, NotificationResponse> onSent) implements PartyNotification {
    }

    public final String sendToSubject(CaseData caseData, String caseNumber) {
        PartyNotification partyNotification = buildSubjectNotification(caseData, caseNumber);
        return partyNotification == null ? null : sendNotification(Party.SUBJECT, caseNumber, caseData, partyNotification);
    }

    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        return null;
    }

    public final String sendToApplicant(CaseData caseData, String caseNumber) {
        PartyNotification partyNotification = buildApplicantNotification(caseData, caseNumber);
        return partyNotification == null ? null : sendNotification(Party.APPLICANT, caseNumber, caseData, partyNotification);
    }

    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        return null;
    }

    public final String sendToRepresentative(CaseData caseData, String caseNumber) {
        PartyNotification partyNotification = buildRepresentativeNotification(caseData, caseNumber);
        return partyNotification == null ? null : sendNotification(Party.REPRESENTATIVE, caseNumber, caseData, partyNotification);
    }

    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        return null;
    }

    public final String sendToRespondent(CaseData caseData, String caseNumber) {
        PartyNotification partyNotification = buildRespondentNotification(caseData, caseNumber);
        return partyNotification == null ? null : sendNotification(Party.RESPONDENT, caseNumber, caseData, partyNotification);
    }

    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        return null;
    }

    public final String sendToTribunal(CaseData caseData, String caseNumber) {
        PartyNotification partyNotification = buildTribunalNotification(caseData, caseNumber);
        return partyNotification == null ? null : sendNotification(Party.TRIBUNAL, caseNumber, caseData, partyNotification);
    }

    protected PartyNotification buildTribunalNotification(CaseData caseData, String caseNumber) {
        return null;
    }

    protected static BiConsumer<CaseData, NotificationResponse> saveToCicCase(
        BiConsumer<CicCase, NotificationResponse> setter
    ) {
        return (caseData, response) -> setter.accept(caseData.getCicCase(), response);
    }

    protected static BiConsumer<CaseData, NotificationResponse> saveToDssData(
        BiConsumer<DssCaseData, NotificationResponse> setter
    ) {
        return (caseData, response) -> setter.accept(caseData.getDssCaseData(), response);
    }

    protected static EmailNotification emailOnly(
        String emailAddress,
        Map<String, Object> templateVars,
        TemplateName templateName,
        BiConsumer<CaseData, NotificationResponse> onSent
    ) {
        return new EmailNotification(emailAddress, templateVars, templateName, new HashMap<>(), new ArrayList<>(), onSent);
    }

    private String sendNotification(Party party, String caseNumber, CaseData caseData, PartyNotification partyNotification) {
        return switch (partyNotification) {
            case EmailNotification emailNotification -> {
                NotificationRequest request = notificationHelper().buildEmailNotificationRequest(emailNotification.emailAddress(),
                    emailNotification.attachedDocuments(),
                    emailNotification.documentTemplateVars(),
                    emailNotification.templateVars(),
                    emailNotification.templateName());
                NotificationResponse response = notificationService().sendEmail(request, caseNumber, party);
                emailNotification.onSent().accept(caseData, response);
                yield response.getId();
            }
            case LetterNotification letterNotification -> {
                notificationHelper().addAddressTemplateVars(letterNotification.postalAddress(), letterNotification.templateVars());
                NotificationRequest request = notificationHelper().buildLetterNotificationRequest(letterNotification.templateVars(),
                    letterNotification.templateName());
                NotificationResponse response = notificationService().sendLetter(request, caseNumber);
                letterNotification.onSent().accept(caseData, response);
                yield response.getId();
            }
        };
    }
}
