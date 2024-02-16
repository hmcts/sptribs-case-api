package uk.gov.hmcts.sptribs.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CicDssUpdateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    private static final EnumSet<State> DSS_UPDATE_CASE_AVAILABLE_STATES = EnumSet.complementOf(EnumSet.of(Draft, DSS_Draft));

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        // IDAM Auth not in use in DSS Update Case so call made by System User
        configBuilder
            .event(CITIZEN_DSS_UPDATE_CASE_SUBMISSION)
            .forStates(DSS_UPDATE_CASE_AVAILABLE_STATES)
            .name("DSS Update Case Submission")
            .description("DSS Update Case Submission")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE,
                CITIZEN,
                CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = addDocumentsToCaseData(details.getData(), details.getData().getDssCaseData());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private CaseData addDocumentsToCaseData(final CaseData caseData, final DssCaseData dssCaseData) {
        final List<CaseworkerCICDocument> docList = new ArrayList<>();

        if (!isEmpty(dssCaseData.getOtherInfoDocuments())) {
            for (ListValue<EdgeCaseDocument> documentListValue : dssCaseData.getOtherInfoDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                doc.setCategoryId(DocumentType.DSS_OTHER.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_OTHER)
                    .build();
                if (!docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }
            }
        }

        final List<ListValue<CaseworkerCICDocument>> docListUpdated = DocumentManagementUtil.buildListValues(docList);
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded =
            isEmpty(caseData.getCicCase().getApplicantDocumentsUploaded())
                ? docListUpdated
                : Stream.concat(
                    caseData.getCicCase().getApplicantDocumentsUploaded().stream(), docListUpdated.stream()).toList();
        caseData.getCicCase().setApplicantDocumentsUploaded(applicantDocumentsUploaded);

        return caseData;
    }
}
