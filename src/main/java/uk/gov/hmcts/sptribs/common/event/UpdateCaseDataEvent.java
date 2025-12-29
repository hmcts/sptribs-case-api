package uk.gov.hmcts.sptribs.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.CaseDataFieldService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_UPDATE_CASE_DATA;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCaseDataEvent implements CCDConfig<CaseData, State, UserRole> {

    private static final String DELETE_FIELD_NOT_FOUND = "The field '%s' was not found in case data. "
        + "Please check the field name and try again.";

    private final CaseDataFieldService caseDataFieldService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_UPDATE_CASE_DATA)
                .forAllStates()
                .name("Update Case Data")
                .description("Update Case Data")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .retries(120, 120)
                .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, SYSTEM_UPDATE);

        final PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        addDeleteFieldPage(pageBuilder);
    }

    private void addDeleteFieldPage(PageBuilder pageBuilder) {
        pageBuilder.page("updateCaseDataDeleteField", this::midEvent)
            .pageLabel("Delete Field from Case Data")
            .label("LabelUpdateCaseDataDeleteField",
                "Warning: This action will permanently remove data from the case. "
                + "Please ensure you have the correct field name before proceeding.")
            .mandatory(CaseData::getDeleteField)
            .optional(CaseData::getDeleteFieldName, "deleteField = \"Yes\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final List<String> errors = new ArrayList<>();

        if (YesNo.YES.equals(caseData.getDeleteField())) {
            final String fieldName = caseData.getDeleteFieldName();

            if (fieldName == null || fieldName.isBlank()) {
                errors.add("Please enter a field name to delete.");
            } else if (!caseDataFieldService.fieldExists(fieldName, caseData)) {
                errors.add(String.format(DELETE_FIELD_NOT_FOUND, fieldName));
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                        final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final List<String> errors = new ArrayList<>();

        if (YesNo.YES.equals(caseData.getDeleteField())) {
            final String fieldName = caseData.getDeleteFieldName();

            if (fieldName != null && !fieldName.isBlank()) {
                try {
                    boolean deleted = caseDataFieldService.deleteField(fieldName, caseData);
                    if (deleted) {
                        log.info("Successfully deleted field '{}' from case data for case {}",
                            fieldName, details.getId());
                    } else {
                        errors.add(String.format("Failed to delete field '%s'. Field not found.", fieldName));
                    }
                } catch (Exception e) {
                    log.error("Error deleting field '{}' from case data for case {}: {}",
                        fieldName, details.getId(), e.getMessage());
                    errors.add(String.format("Error deleting field '%s': %s", fieldName, e.getMessage()));
                }
            }
        }

        // Clear the temporary fields
        caseData.setDeleteField(null);
        caseData.setDeleteFieldName(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .errors(errors)
            .build();
    }
}
