package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.OrdersListRestoreService;

import java.time.LocalDate;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@RequiredArgsConstructor
@Component
@Slf4j
public class SystemRestoreOrders implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_RESTORE_ORDERS = "system-restore-orders";

    private static final LocalDate START_FROM_DATE = LocalDate.of(2026, 2, 24);

    private static final LocalDate END_TO_DATE = LocalDate.of(2026, 3, 5);

    private final OrdersListRestoreService ordersListRestoreService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_RESTORE_ORDERS)
            .forAllStates()
            .name("Repopulate missing orders")
            .description("Recover orders that are missing in cicCase")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = caseDetails.getData();
        Long reference = caseDetails.getId();

        ordersListRestoreService.restoreOrdersList(reference, caseData, START_FROM_DATE, END_TO_DATE);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
