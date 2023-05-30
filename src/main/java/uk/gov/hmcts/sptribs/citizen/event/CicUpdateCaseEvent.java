package uk.gov.hmcts.sptribs.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SchemeCic;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;
import uk.gov.hmcts.sptribs.services.CaseManagementService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicUpdateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    AppsConfig appsConfig;

    @Autowired
    CaseManagementService caseManagementService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                .getUpdateEvent())
            .forStates(State.Draft, State.Submitted)
            .name("Update case (cic)")
            .description("Application update (cic)")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN_CIC)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        var data = details.getData();
        var dssData = details.getData().getDssCaseData();
        CaseData caseData = getCaseData(data, dssData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    private CaseData getCaseData(final CaseData caseData, final DssCaseData dssCaseData) {
        caseData.getCicCase().setSchemeCic(SchemeCic.Year2012);
        caseData.getCicCase().setFullName(dssCaseData.getSubjectFullName());
        caseData.getCicCase().setDateOfBirth(dssCaseData.getSubjectDateOfBirth());
        caseData.getCicCase().setEmail(dssCaseData.getSubjectEmailAddress());
        caseData.getCicCase().setPhoneNumber(dssCaseData.getSubjectContactNumber());
        caseData.getCicCase().getPartiesCIC().add(PartiesCIC.SUBJECT);
        caseData.getCicCase().getSubjectCIC().add(SubjectCIC.SUBJECT);
        caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        if (!ObjectUtils.isEmpty(dssCaseData.getRepresentativeFullName())) {
            caseData.getCicCase().setRepresentativeFullName(dssCaseData.getRepresentativeFullName());
            caseData.getCicCase().setRepresentativeOrgName(dssCaseData.getRepresentativeOrganisationName());
            caseData.getCicCase().setRepresentativePhoneNumber(dssCaseData.getRepresentativeContactNumber());
            caseData.getCicCase().setRepresentativeEmailAddress(dssCaseData.getRepresentativeEmailAddress());
            caseData.getCicCase().setIsRepresentativeQualified(dssCaseData.getRepresentationQualified());
            caseData.getCicCase().getPartiesCIC().add(PartiesCIC.REPRESENTATIVE);
        }
        caseData.getCicCase().setCaseReceivedDate(LocalDate.now());
        List<CaseworkerCICDocument> docList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dssCaseData.getOtherInfoDocuments())) {
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

        if (!CollectionUtils.isEmpty(dssCaseData.getSupportingDocuments())) {
            for (ListValue<EdgeCaseDocument> documentListValue : dssCaseData.getSupportingDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                doc.setCategoryId(DocumentType.DSS_SUPPORTING.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_SUPPORTING)
                    .build();
                if (!docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }
            }
        }

        if (!CollectionUtils.isEmpty(dssCaseData.getTribunalFormDocuments())) {
            for (ListValue<EdgeCaseDocument> documentListValue : dssCaseData.getTribunalFormDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                doc.setCategoryId(DocumentType.DSS_TRIBUNAL_FORM.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_TRIBUNAL_FORM)
                    .build();
                if (!docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }
            }
        }
        caseData.getCicCase().setApplicantDocumentsUploaded(DocumentManagementUtil.buildListValues(docList));
        dssCaseData.setTribunalFormDocuments(new ArrayList<>());
        dssCaseData.setSupportingDocuments(new ArrayList<>());
        dssCaseData.setOtherInfoDocuments(new ArrayList<>());
        caseData.setDssCaseData(dssCaseData);
        return caseData;
    }
}
