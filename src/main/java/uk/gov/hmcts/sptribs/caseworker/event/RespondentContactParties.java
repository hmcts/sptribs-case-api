
package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ContactPartiesSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.event.page.RespondentPartiesToContact;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.ContactPartiesNotification;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;


@Component
@Slf4j
@Setter
public class RespondentContactParties implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration resPartiesToContact = new RespondentPartiesToContact();
    private static final CcdPageConfiguration contactPartiesSelectDocument = new ContactPartiesSelectDocument();

    @Value("${case_document_am.url}")
    private String baseUrl;


    @Autowired
    private ContactPartiesNotification contactPartiesNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(RESPONDENT_CONTACT_PARTIES)
                .forStates(Draft,
                    Withdrawn,
                    Rejected,
                    Submitted,
                    NewCaseReceived,
                    CaseManagement,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Case: CICA Contact parties")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .aboutToStartCallback(this::aboutToStart)
                .submittedCallback(this::partiesContacted)
                .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_RESPONDENT)
                .grantHistoryOnly(
                    ST_CIC_CASEWORKER,
                    ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_SENIOR_JUDGE,
                    SUPER_USER,
                    ST_CIC_JUDGE,
                    ST_CIC_RESPONDENT)
        );
        contactPartiesSelectDocument.addTo(pageBuilder);
        resPartiesToContact.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        DynamicMultiSelectList documentList = DocumentListUtil.prepareDocumentList(caseData,baseUrl);
        caseData.getContactPartiesDocuments().setDocumentList(documentList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse partiesContacted(CaseDetails<CaseData, State> details,
                                                      CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        String caseNumber = data.getHyphenatedCaseRef();
        try {
            sendContactPartiesNotification(details, data, caseNumber);
        } catch (Exception notificationException) {
            log.error("Contact Parties notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Contact Parties notification failed %n## Please resend again"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Message sent %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getContactParties())
            ))
            .build();

    }

    private void sendContactPartiesNotification(CaseDetails<CaseData, State> details, CaseData data, String caseNumber) {
        if (!CollectionUtils.isEmpty(data.getContactParties().getSubjectContactParties())) {
            contactPartiesNotification.sendToSubject(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getContactParties().getRepresentativeContactParties())) {
            contactPartiesNotification.sendToRepresentative(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getContactParties().getApplicantContactParties())) {
            contactPartiesNotification.sendToApplicant(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getContactParties().getTribunal())) {
            contactPartiesNotification.sendToTribunal(details.getData(), caseNumber);
        }
    }

}

