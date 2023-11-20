package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.LinkCaseSelectCase;
import uk.gov.hmcts.sptribs.caseworker.model.CaseLinks;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_LINK_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseWorkerLinkCase implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.link-case.enabled}")
    private boolean linkCaseEnabled;

    private static final CcdPageConfiguration linkCaseSelectCase = new LinkCaseSelectCase();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (linkCaseEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_LINK_CASE)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome)
            .name("Links: Link case")
            .showSummary()
            .description("Links: Link case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER));

        addWarning(pageBuilder);
        linkCaseSelectCase.addTo(pageBuilder);
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker link the case callback invoked for Case Id: {}", details.getId());
        var data = details.getData();

        CaseLinks caseLink = CaseLinks.builder()
            .caseReference(data.getCicCase().getLinkCaseNumber())
            .reason(data.getCicCase().getLinkCaseReason().getLabel())
            .otherDescription(data.getCicCase().getLinkCaseOtherDescription())
            .build();

        if (CollectionUtils.isEmpty(data.getCicCase().getCaseLinks())) {
            List<ListValue<CaseLinks>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<CaseLinks>builder()
                .id("1")
                .value(caseLink)
                .build();

            listValues.add(listValue);

            data.getCicCase().setCaseLinks(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<CaseLinks>builder()
                .value(caseLink)
                .build();

            data.getCicCase().getCaseLinks().add(0, listValue); // always add new note as first element so that it is displayed on top

            data.getCicCase().getCaseLinks().forEach(
                caseNoteListValue -> caseNoteListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(MessageUtil.generateSimpleMessage(
                "Link added to case", ""))
            .build();
    }

    private void addWarning(PageBuilder pageBuilder) {
        pageBuilder.page("beforeYouStart")
            .pageLabel("Before you start")
            .label("beforeYouStartLabel",
                """
                    If a group of linked cases has a lead case, you must start from the lead case.

                    If the cases to be linked has no lead, you can start the linking journey from any of those cases
                    """);
    }


}
