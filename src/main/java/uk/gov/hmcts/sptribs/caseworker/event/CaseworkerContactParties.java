package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.EventMetadata;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.PartiesToContact;
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.notification.NotificationConstantProfiles;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.dispatcher.ContactPartiesNotification;
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;


@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class CaseworkerContactParties implements CCDConfig<CaseData, State, UserRole> {

    @Value("${case-api.url}")
    private String baseUrl;

    private static final CcdPageConfiguration partiesToContact = new PartiesToContact();
    private final ContactPartiesSelectDocument contactPartiesSelectDocument;

    private final ContactPartiesNotification contactPartiesNotification;
    private final NotificationDispatcher notificationDispatcher;
    private final NotificationHelper notificationHelper;
    private final ContactPartiesService contactPartiesService;
    private static final int DOC_ATTACH_LIMIT = 10;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_CONTACT_PARTIES)
                .forStates(Draft,
                    Withdrawn,
                    Rejected,
                    Submitted,
                    NewCaseReceived,
                    CaseManagement,
                    ReadyToList,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Case: Contact parties")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(
                    ST_CIC_CASEWORKER,
                    ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_SENIOR_JUDGE,
                    SUPER_USER,
                    ST_CIC_JUDGE)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        contactPartiesSelectDocument.addTo(pageBuilder);
        partiesToContact.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        caseData.setContactParties(new ContactParties());
        DynamicMultiSelectList documentList = DocumentListUtil.prepareContactPartiesDocumentList(caseData, baseUrl);
        caseData.getContactPartiesDocuments().setDocumentList(documentList);
        caseData.getCicCase().setNotifyPartyMessage("");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        StringBuilder sentDocListBuilder = new StringBuilder();

        sentDocListBuilder.append("| ");

        caseData.getContactPartiesDocuments().getDocumentList().getValue()
            .forEach(doc -> {
                sentDocListBuilder.append(extractDocNameAndCategory(doc.getLabel())).append(" | ");
            });

        int numberOfSentDocs = caseData.getContactPartiesDocuments().getDocumentList().getValue().size();

        String sentDocList = sentDocListBuilder.toString();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .eventMetadata(EventMetadata.builder().summary(numberOfSentDocs +  " Selected documents sent").description(sentDocList).build())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        String caseNumber = caseData.getHyphenatedCaseRef();

        final Map<String, String> uploadedDocuments = notificationHelper
            .buildDocumentList(caseData.getContactPartiesDocuments().getDocumentList(), DOC_ATTACH_LIMIT);

        NotificationContextRequest request = NotificationContextRequest.builder()
            .caseData(caseData)
            .caseReference(caseNumber)
            .notification(contactPartiesNotification)
            .uploadedDocuments(uploadedDocuments)
            .build();

        NotificationContext notificationContext = NotificationConstantProfiles.CONTACT_PARTIES.buildContext(
            request);

        notificationDispatcher.sendToCorrespondenceParties(notificationContext);

        if (isEmpty(notificationContext.getErrors())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Message sent %n## %s",
                    MessageUtil.generateSimpleMessageFromCorrespondenceParties(notificationContext.getCorrespondenceParties())))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Contact Parties notification failed %n## %s %n## Please resend the notification. ",
                        generateSimpleErrorMessage(notificationContext.getErrors()))
                )
                .build();
        }
    }


    private String extractDocNameAndCategory(String label) {

        Pattern pattern = Pattern.compile("\\[([^]]+)]");
        Matcher matcher = pattern.matcher(label);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return label;
    }
}
