package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.StatementSelectParty;
import uk.gov.hmcts.sptribs.caseworker.event.page.StatementUploadDocument;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;
import uk.gov.hmcts.sptribs.statement.service.StatementPersistenceException;
import uk.gov.hmcts.sptribs.statement.service.StatementService;
import uk.gov.hmcts.sptribs.statement.service.StatementValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_UPLOAD_STATEMENT;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerUploadStatement implements CCDConfig<CaseData, State, UserRole> {

    private static final String APPLICANT = "Applicant";
    private static final String RESPONDENT = "Respondent";
    private static final String REPRESENTATIVE = "Representative";
    private static final String STATEMENT_SAVE_ERROR = "Unable to save statement right now. Please try again.";

    private final StatementSelectParty statementSelectParty;
    private final StatementUploadDocument statementUploadDocument;
    private final StatementService statementService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_UPLOAD_STATEMENT)
                .forStates(Withdrawn,
                    Rejected,
                    Submitted,
                    NewCaseReceived,
                    CaseManagement,
                    ReadyToList,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Case docs: Upload statement")
                .description("Case docs: Upload statement")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_CASEWORKER,
                    ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        statementSelectParty.addTo(pageBuilder);
        statementUploadDocument.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        StatementUpload statementUpload = caseData.getStatementUpload();
        if (statementUpload == null) {
            statementUpload = new StatementUpload();
            caseData.setStatementUpload(statementUpload);
        }

        statementUpload.setStatementParty(buildStatementPartyList(caseData));
        statementUpload.setStatementDocument(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData caseData = details.getData();
        try {
            statementService.saveStatement(details.getId(), caseData);
        } catch (StatementValidationException validationException) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(validationException.getMessage()))
                .build();
        } catch (StatementPersistenceException persistenceException) {
            log.error("Failed to save statement for case {}", details.getId(), persistenceException);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(STATEMENT_SAVE_ERROR))
                .build();
        } catch (RuntimeException runtimeException) {
            log.error("Unexpected error while saving statement for case {}", details.getId(), runtimeException);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(STATEMENT_SAVE_ERROR))
                .build();
        }

        caseData.setStatementUpload(new StatementUpload());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Statement uploaded")
            .build();
    }

    private DynamicList buildStatementPartyList(CaseData caseData) {
        List<DynamicListElement> parties = new ArrayList<>();

        if (StringUtils.isNotBlank(caseData.getCicCase().getApplicantFullName())) {
            parties.add(createElement(APPLICANT));
        }

        if (StringUtils.isNotBlank(caseData.getCicCase().getRespondentName())) {
            parties.add(createElement(RESPONDENT));
        }

        if (StringUtils.isNotBlank(caseData.getCicCase().getRepresentativeFullName())) {
            parties.add(createElement(REPRESENTATIVE));
        }

        return DynamicList.builder()
            .listItems(parties)
            .build();
    }

    private DynamicListElement createElement(String label) {
        return DynamicListElement.builder()
            .code(UUID.randomUUID())
            .label(label)
            .build();
    }

}
