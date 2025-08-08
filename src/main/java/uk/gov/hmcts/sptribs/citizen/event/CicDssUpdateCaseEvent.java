package uk.gov.hmcts.sptribs.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssMessage;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.dispatcher.DssUpdateCaseSubmissionNotification;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Expired;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
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

@Slf4j
@Component
public class CicDssUpdateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    private static final EnumSet<State> DSS_UPDATE_CASE_AVAILABLE_STATES = EnumSet.complementOf(EnumSet.of(Draft, DSS_Draft, DSS_Expired));

    @Autowired
    private DssUpdateCaseSubmissionNotification dssUpdateCaseSubmissionNotification;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
                .forStates(DSS_UPDATE_CASE_AVAILABLE_STATES)
                .name("DSS Update Case Submission")
                .description("DSS Update Case Submission")
                .retries(120, 120)
                .grant(CREATE_READ_UPDATE_DELETE, CITIZEN, CREATOR)
                .grantHistoryOnly(
                    ST_CIC_CASEWORKER,
                    ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_SENIOR_JUDGE,
                    SUPER_USER,
                    ST_CIC_JUDGE,
                    SYSTEM_UPDATE
                )
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .publishToCamunda()
                .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = addDocumentsToCaseData(details.getData(), details.getData().getDssCaseData());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private CaseData addDocumentsToCaseData(final CaseData caseData, final DssCaseData dssCaseData) {
        final List<CaseworkerCICDocument> documentList = new ArrayList<>();
        final List<ListValue<DssMessage>> messagesList = new ArrayList<>();

        if (!isEmpty(dssCaseData.getOtherInfoDocuments())) {
            for (ListValue<CitizenCICDocument> documentListValue : dssCaseData.getOtherInfoDocuments()) {
                Document document = documentListValue.getValue().getDocumentLink();
                String documentComment = documentListValue.getValue().getComment();
                document.setCategoryId(DocumentType.DSS_OTHER.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(document)
                    .documentEmailContent(documentComment)
                    .documentCategory(DocumentType.DSS_OTHER)
                    .date(LocalDate.now())
                    .build();
                if (!documentList.contains(caseworkerCICDocument)) {
                    documentList.add(caseworkerCICDocument);
                }
            }
        }

        if (isNotBlank(dssCaseData.getAdditionalInformation())) {
            final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));

            final DssMessage message = DssMessage.builder()
                .message(dssCaseData.getAdditionalInformation())
                .dateReceived(LocalDate.now())
                .receivedFrom(user.getUserDetails().getFullName())
                .build();

            final ListValue<DssMessage> listValue = ListValue
                .<DssMessage>builder()
                .id(UUID.randomUUID().toString())
                .value(message)
                .build();

            messagesList.add(listValue);

            final List<ListValue<DssMessage>> updatedMessagesList =
                isEmpty(caseData.getMessages())
                    ? messagesList
                    : Stream.concat(caseData.getMessages().stream(), messagesList.stream()).toList();
            caseData.setMessages(updatedMessagesList);
        }

        final List<ListValue<CaseworkerCICDocument>> documentListUpdated = DocumentManagementUtil.buildListValues(documentList);
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded =
            isEmpty(caseData.getCicCase().getApplicantDocumentsUploaded())
                ? documentListUpdated
                : Stream.concat(
                    caseData.getCicCase().getApplicantDocumentsUploaded().stream(), documentListUpdated.stream()).toList();
        caseData.getCicCase().setApplicantDocumentsUploaded(applicantDocumentsUploaded);

        dssCaseData.setOtherInfoDocuments(new ArrayList<>());
        dssCaseData.setAdditionalInformation(null);
        caseData.setDssCaseData(dssCaseData);

        return caseData;
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        try {
            dssUpdateCaseSubmissionNotification.sendToApplicant(details.getData(), String.valueOf(details.getId()));
            dssUpdateCaseSubmissionNotification.sendToTribunal(details.getData(), String.valueOf(details.getId()));
        } catch (Exception notificationException) {
            log.error("CIC Dss Update Case Event Email notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader("# CIC Dss Update Case Event Email notification failed %n## Please resend the notification")
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# CIC Dss Update Case Event Email notifications sent")
            .build();
    }
}
