package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.CaseNote;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ADD_NOTE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerAddNote implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private Clock clock;

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ADD_NOTE)
                .forAllStates()
                .name("Case: Add note")
                .description("Case: Add note")
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, SUPER_USER);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                        .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }

        new PageBuilder(eventBuilder)
            .page("addCaseNotes")
            .pageLabel("Add case notes")
            .optional(CaseData::getNote);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));

        final CaseData caseData = details.getData();

        String note = caseData.getNote();

        final CaseNote caseNote = new CaseNote();
        caseNote.setNote(note);
        caseNote.setDate(LocalDate.now(clock));
        caseNote.setAuthor(caseworkerUser.getUserDetails().getFullName());

        if (isEmpty(caseData.getNotes())) {
            List<ListValue<CaseNote>> listValues = new ArrayList<>();

            final ListValue<CaseNote> listValue = ListValue
                .<CaseNote>builder()
                .id("1")
                .value(caseNote)
                .build();

            listValues.add(listValue);

            caseData.setNotes(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            final ListValue<CaseNote> listValue = ListValue
                .<CaseNote>builder()
                .value(caseNote)
                .build();

            caseData.getNotes().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getNotes().forEach(caseNoteListValue -> caseNoteListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }

        caseData.setNote(null); //Clear note text area as notes value is stored in notes collection

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
