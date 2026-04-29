package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.*;
import uk.gov.hmcts.sptribs.DTO.*;
import uk.gov.hmcts.sptribs.caseworker.event.page.*;
import uk.gov.hmcts.sptribs.caseworker.model.*;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.*;
import uk.gov.hmcts.sptribs.service.*;

import java.util.*;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateUploadedDocuments;

@Component
@Slf4j
public class CaseworkerUploadStatement implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration selectParty = new HearingStatementPartySelection();
    private static final CcdPageConfiguration uploadDocument = new UploadHearingStatementDocument();

    @Autowired
    private StatementService statementService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = uploadStatement(configBuilder);
        selectParty.addTo(pageBuilder);
        uploadDocument.addTo(pageBuilder);
    }

    public PageBuilder uploadStatement(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder.event("caseworker-upload-statement")
                .forAllStates()
                .name("Upload Statement")
                .description("Upload a party statement")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER);

        return new PageBuilder(eventBuilder);
    }

//    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
//        var caseData = details.getData();
//        var cicCase = caseData.getCicCase();
//
//        DynamicList parties = createDynamicList(List.of(APPLICANT, RESPONDENT, REPRESENTATIVE));
//
//        cicCase.setWhichPartyStatementDynamicRadioList(parties);
//
//        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
//            .data(caseData)
//            .build();
//
//    }
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        statementService.saveStatement(StatementDTO.builder()
            .caseReferenceNumber(details.getId())
            .partyType(caseData.getStatementRecord().getPartyType())
            .documentUrl(caseData.getNewDocManagement().getHearingStatementCICDocument().getDocumentLink().getUrl())
            .documentFilename(caseData.getNewDocManagement().getHearingStatementCICDocument().getDocumentLink().getFilename())
            .documentBinaryUrl(caseData.getNewDocManagement().getHearingStatementCICDocument().getDocumentLink().getBinaryUrl())
            .build());

        caseData.setStatementRecord(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }



//    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
//        final CaseDetails<CaseData, State> details,
//        final CaseDetails<CaseData, State> beforeDetails
//    ) {
//
//        final CaseData caseData = details.getData();
//        AcknowledgementCICDocument acknowledgmentDoc =
//            caseData.getNewDocManagement().getAcknowledgementCICDocument();
//
//        try {
//
//            statementService.uploadPartyStatement(acknowledgmentDoc,
//                caseData.getCicCase().getWhichPartyStatementDynamicRadioList().getValue().getLabel(),
//                caseData.getHyphenatedCaseRef());
//
//        } catch (Exception notificationException) {
//            log.error("Exception When uploading to database", notificationException);
//        }
//
//        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
//            .data(caseData)
//            .build();
//    }

//    public static DynamicList createDynamicList(List<String> parties) {
//        List<DynamicListElement> dynamicListElements = parties
//            .stream()
//            .map(party -> DynamicListElement.builder().label(party).code(UUID.randomUUID()).build())
//            .toList();
//
//        return DynamicList
//            .builder()
//            .listItems(dynamicListElements)
//            .build();
//    }



}
