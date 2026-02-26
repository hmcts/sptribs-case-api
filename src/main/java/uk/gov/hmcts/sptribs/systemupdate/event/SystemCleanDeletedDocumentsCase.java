package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemCleanDeletedDocumentsCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_CLEAN_DELETED_DOCUMENTS = "system-clean-deleted-documents";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder = configBuilder
            .event(SYSTEM_CLEAN_DELETED_DOCUMENTS)
            .forAllStates()
            .name("Clean deleted documents")
            .description("Clean deleted documents that are stuck in further documents")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE)
            // ????
            .publishToCamunda()
            .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {


        log.info("Clean deleted documents event about to clear stuck documents for caseId = {}",
            caseDetails.getId());

        final CaseData caseData = caseDetails.getData();
        //clean logic here::::

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
