package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.ASYNC_STITCH_COMPLETE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerBundleStitchComplete implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALWAYS_HIDE = "[STATE]=\"ALWAYS_HIDE\"";

    @Autowired
    private DocumentsService documentsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(ASYNC_STITCH_COMPLETE)
            .forStates(CaseManagement, AwaitingHearing, ReadyToList, CaseClosed)
            .name("Bundle: Async Stitching Comp")
            .description("Bundle: Async Stitching Comp")
            .showCondition(ALWAYS_HIDE)
            .showSummary()
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE))
            .page("createBundle")
            .pageLabel("Create a bundle")
            .done();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = details.getId();

        log.info("Starting insert for latest case bundle for caseId = {}", caseId);

        Document stitchedDocument = details.getData()
            .getCaseBundles()
            .getFirst()
            .getValue()
            .getStitchedDocument();

        if (stitchedDocument == null) {
            log.info("No stitched document found for latest bundle, caseId = {}", caseId);
            return buildResponse("# No stitched bundle document found");
        }

        try {

            saveLatestBundleDocument(stitchedDocument, caseId);

            log.info("Successfully inserted latest case bundle document for caseId = {}", caseId);

            return buildResponse("# documents added successfully");

        } catch (RuntimeException exception) {
            log.error("Error inserting latest case bundle document for caseId = {}", caseId, exception);

            return buildResponse("# Error saving latest case bundle to document entity");
        }
    }

    private SubmittedCallbackResponse buildResponse(String confirmationHeader) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(confirmationHeader)
            .build();
    }

    private void saveLatestBundleDocument(Document stitchedDocument, Long caseId) {
        documentsService.buildAndSaveNewDocumentEntity(
            stitchedDocument,
            caseId,
            false,
            true
        );
    }
}
