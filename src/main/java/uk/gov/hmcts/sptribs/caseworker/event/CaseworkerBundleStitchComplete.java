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

        CaseData caseData = details.getData();

        ListValue<Bundle> latestCaseBundle = caseData.getCaseBundles().getFirst();
        Document stitchedDocument = latestCaseBundle.getValue().getStitchedDocument();

        if (stitchedDocument != null) {
            log.info("Inserting latest bundle document for caseId = {}", caseId);

            documentsService.buildAndSaveNewDocumentEntity(
                stitchedDocument,
                caseId,
                false,
                true
            );

            log.info("Successfully inserted latest case bundle document for caseId = {}", caseId);
        } else {
            log.info("No stitched document found for caseId = {}, skipping insert", caseId);
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# documents added successfully")
            .build();
    }
}
