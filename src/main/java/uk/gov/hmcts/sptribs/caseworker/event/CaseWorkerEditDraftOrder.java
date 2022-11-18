package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.CreateDraftOrder;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerEditDraftOrder implements CCDConfig<CaseData, State, UserRole> {
    private static final CcdPageConfiguration editDraftOrder = new CreateDraftOrder();
    private static final CcdPageConfiguration previewDraftOrder = new PreviewDraftOrder();

    public static final String CASEWORKER_EDIT_DRAFT_ORDER = "caseworker-edit-draft-order";


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_EDIT_DRAFT_ORDER)
                .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
                .name("Edit draft order")
                .showSummary()
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));

        editDraftOrder.addTo(pageBuilder);
        previewDraftOrder.addTo(pageBuilder);


    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details
    ) {

        var caseData = details.getData();
        var draftOrder = caseData.getDraftOrderCIC();

        if (isEmpty(caseData.getDraftOrderCICList())) {
            List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<DraftOrderCIC>builder()
                .id("1")
                .value(draftOrder)
                .build();

            listValues.add(listValue);

            caseData.setDraftOrderCICList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<DraftOrderCIC>builder()
                .value(draftOrder)
                .build();

            caseData.getDraftOrderCICList().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getDraftOrderCICList().forEach(
                caseDraftOrderCic -> caseDraftOrderCic.setId(String.valueOf(listValueIndex.incrementAndGet()))
            );

        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }



}
