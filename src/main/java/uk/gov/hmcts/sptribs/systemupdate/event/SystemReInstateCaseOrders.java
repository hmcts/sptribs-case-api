package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemReInstateCaseOrders implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REPLACE_ORDERS = "system-replace-orders";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_REPLACE_ORDERS)
            .forAllStates()
            .name("Repopulate missing orders ")
            .description("Replace orders that are missing in cicCase")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = caseDetails.getData();

        //if order is missing we should still have the orders here, if they have only removed 1 doc ***
        // if they did it twice we have a bigger problem
        //this is still only the document we are missing fields from the order object, like date, sent date, parties...
        List<ListValue<CaseworkerCICDocument>> missingOrders = caseData.getCicCase().getOrderDocumentList();


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


}
