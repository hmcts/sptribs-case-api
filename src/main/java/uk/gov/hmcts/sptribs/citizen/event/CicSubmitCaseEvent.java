package uk.gov.hmcts.sptribs.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssMessage;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@Setter
public class CicSubmitCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    private HttpServletRequest request;
    private IdamService idamService;
    private AppsConfig appsConfig;
    private DssApplicationReceivedNotification dssApplicationReceivedNotification;

    @Autowired
    public CicSubmitCaseEvent(HttpServletRequest request, IdamService idamService, AppsConfig appsConfig,
                              DssApplicationReceivedNotification dssApplicationReceivedNotification) {
        this.request = request;
        this.idamService = idamService;
        this.appsConfig = appsConfig;
        this.dssApplicationReceivedNotification = dssApplicationReceivedNotification;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                    .getSubmitEvent())
                .forStateTransition(DSS_Draft, DSS_Submitted)
                .name("Submit case (cic)")
                .description("Application submit (cic)")
                .retries(120, 120)
                .grant(CREATE_READ_UPDATE_DELETE, CITIZEN)
                .grant(CREATE_READ_UPDATE, SYSTEM_UPDATE, CREATOR, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(
                    ST_CIC_CASEWORKER,
                    ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_SENIOR_JUDGE,
                    SUPER_USER,
                    ST_CIC_JUDGE)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .publishToCamunda();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData data = details.getData();
        final DssCaseData dssData = details.getData().getDssCaseData();
        final CaseData caseData = getCaseData(data, dssData);
        setDssMetaData(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.DSS_Submitted)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData data = details.getData();
        DssCaseData dssCaseData = data.getDssCaseData();
        generateNotifyParties(dssCaseData);

        final String caseNumber = data.getHyphenatedCaseRef();

        try {
            sendApplicationReceivedNotification(caseNumber, data);
        } catch (Exception notificationException) {
            log.error("Application Received notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader("# Application Received notification failed %n## Please resend the notification")
                .build();
        }

        if (isNotEmpty(dssCaseData.getNotificationParties())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Application Received %n## %s",
                    MessageUtil.generateSimpleMessage(dssCaseData.getNotificationParties())))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Application Received %n##")
            .build();
    }

    private void setDssMetaData(CaseData data) {
        data.setDssQuestion1("Full Name");
        data.setDssQuestion2("Date of Birth");
        data.setDssAnswer1("case_data.dssCaseDataSubjectFullName");
        data.setDssAnswer2("case_data.dssCaseDataSubjectDateOfBirth");
        data.setDssHeaderDetails("Subject of this case");
    }

    private void generateNotifyParties(DssCaseData dssCaseData) {
        Set<NotificationParties> notificationParties = new HashSet<>();

        if (dssCaseData.getSubjectEmailAddress() != null) {
            notificationParties.add(NotificationParties.SUBJECT);
        }

        if (dssCaseData.getRepresentativeEmailAddress() != null) {
            notificationParties.add(NotificationParties.REPRESENTATIVE);
        }

        dssCaseData.setNotificationParties(notificationParties);
    }

    private void sendApplicationReceivedNotification(String caseNumber, CaseData caseData) {
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        if (dssCaseData.getNotificationParties().contains(NotificationParties.SUBJECT)) {
            dssApplicationReceivedNotification.sendToSubject(caseData, caseNumber);
        }

        if (dssCaseData.getNotificationParties().contains(NotificationParties.REPRESENTATIVE)) {
            dssApplicationReceivedNotification.sendToRepresentative(caseData, caseNumber);
        }
    }

    private CaseData getCaseData(final CaseData caseData, final DssCaseData dssCaseData) {
        caseData.getCicCase().setCaseReceivedDate(LocalDate.now());
        caseData.getCicCase().setInitialCicaDecisionDate(LocalDate.now());
        caseData.getCicCase().calculateAndSetIsCaseInTime(caseData);
        caseData.getCicCase().setFullName(dssCaseData.getSubjectFullName());
        caseData.getCicCase().setDateOfBirth(dssCaseData.getSubjectDateOfBirth());
        caseData.getCicCase().setEmail(dssCaseData.getSubjectEmailAddress());
        caseData.getCicCase().setPhoneNumber(dssCaseData.getSubjectContactNumber());
        caseData.setCaseNameHmctsInternal(dssCaseData.getSubjectFullName());
        caseData.setCaseFlags(Flags.builder()
            .details(new ArrayList<>())
            .build());
        caseData.setSubjectFlags(Flags.builder()
            .details(new ArrayList<>())
            .partyName(dssCaseData.getSubjectFullName())
            .roleOnCase("subject")
            .build()
        );

        final Set<PartiesCIC> setParty = new HashSet<>();
        setParty.add(PartiesCIC.SUBJECT);
        caseData.getCicCase().setPartiesCIC(setParty);

        final Set<SubjectCIC> set = new HashSet<>();
        set.add(SubjectCIC.SUBJECT);
        caseData.getCicCase().setSubjectCIC(set);
        caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);

        if (!ObjectUtils.isEmpty(dssCaseData.getRepresentativeFullName())) {
            caseData.getCicCase().setRepresentativeFullName(dssCaseData.getRepresentativeFullName());
            caseData.getCicCase().setRepresentativeOrgName(dssCaseData.getRepresentativeOrganisationName());
            caseData.getCicCase().setRepresentativePhoneNumber(dssCaseData.getRepresentativeContactNumber());
            caseData.getCicCase().setRepresentativeEmailAddress(dssCaseData.getRepresentativeEmailAddress());
            caseData.getCicCase().setIsRepresentativeQualified(dssCaseData.getRepresentationQualified());
            caseData.getCicCase().getPartiesCIC().add(PartiesCIC.REPRESENTATIVE);
            final Set<RepresentativeCIC> setRep = new HashSet<>();
            setRep.add(RepresentativeCIC.REPRESENTATIVE);
            caseData.getCicCase().setRepresentativeCIC(setRep);
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            caseData.setRepresentativeFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(dssCaseData.getRepresentativeFullName())
                .roleOnCase("Representative")
                .build()
            );
        }

        List<CaseworkerCICDocument> docList = new ArrayList<>();

        if (isNotEmpty(dssCaseData.getOtherInfoDocuments())) {
            for (ListValue<CitizenCICDocument> documentListValue : dssCaseData.getOtherInfoDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                String documentComment = documentListValue.getValue().getComment();
                doc.setCategoryId(DocumentType.DSS_OTHER.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_OTHER)
                    .documentEmailContent(documentComment)
                    .date(LocalDate.now())
                    .build();

                if (!docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }
            }
        }

        if (isNotBlank(dssCaseData.getAdditionalInformation())) {
            final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
            final DssMessage message = DssMessage.builder()
                .message(dssCaseData.getAdditionalInformation())
                .dateReceived(LocalDate.now())
                .receivedFrom(caseworkerUser.getUserDetails().getFullName())
                .build();

            final ListValue<DssMessage> listValue = ListValue
                .<DssMessage>builder()
                .id(UUID.randomUUID().toString())
                .value(message)
                .build();
            List<ListValue<DssMessage>> messagesList = new ArrayList<>();
            messagesList.add(listValue);
            caseData.setMessages(messagesList);
        }

        if (isNotEmpty(dssCaseData.getSupportingDocuments())) {
            for (ListValue<CitizenCICDocument> documentListValue : dssCaseData.getSupportingDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                doc.setCategoryId(DocumentType.DSS_SUPPORTING.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_SUPPORTING)
                    .date(LocalDate.now())
                    .build();
                if (!docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }
            }
        }

        if (isNotEmpty(dssCaseData.getTribunalFormDocuments())) {
            for (ListValue<CitizenCICDocument> documentListValue : dssCaseData.getTribunalFormDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                doc.setCategoryId(DocumentType.DSS_TRIBUNAL_FORM.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_TRIBUNAL_FORM)
                    .date(LocalDate.now())
                    .build();
                if (!docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }
            }
        }

        caseData.getCicCase().setApplicantDocumentsUploaded(DocumentManagementUtil.buildListValues(docList));
        dssCaseData.setTribunalFormDocuments(new ArrayList<>());
        dssCaseData.setSupportingDocuments(new ArrayList<>());
        dssCaseData.setOtherInfoDocuments(new ArrayList<>());
        caseData.setDssCaseData(dssCaseData);
        return caseData;
    }
}
