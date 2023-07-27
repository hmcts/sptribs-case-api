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
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssMessage;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicSubmitCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    AppsConfig appsConfig;

    @Autowired
    private DssApplicationReceivedNotification dssApplicationReceivedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                .getSubmitEvent())
            .forStates(State.DSS_Draft)
            .name("Submit case (cic)")
            .description("Application submit (cic)")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN_CIC, CREATOR).grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE,
                CITIZEN_CIC,
                CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        var dssData = details.getData().getDssCaseData();
        CaseData caseData = getCaseData(data, dssData);
        String caseNumber = data.getHyphenatedCaseRef();

        sendApplicationReceivedNotification(caseNumber, data);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.DSS_Submitted)
            .build();
    }

    private CaseData getCaseData(final CaseData caseData, final DssCaseData dssCaseData) {
        caseData.getCicCase().setCaseReceivedDate(LocalDate.now());
        caseData.getCicCase().setFullName(dssCaseData.getSubjectFullName());
        caseData.getCicCase().setDateOfBirth(dssCaseData.getSubjectDateOfBirth());
        caseData.getCicCase().setEmail(dssCaseData.getSubjectEmailAddress());
        caseData.getCicCase().setPhoneNumber(dssCaseData.getSubjectContactNumber());
        final Set<PartiesCIC> setParty = new HashSet<>();
        setParty.add(PartiesCIC.SUBJECT);
        caseData.getCicCase().setPartiesCIC(setParty);
        final Set<SubjectCIC> set = new HashSet<>();
        set.add(SubjectCIC.SUBJECT);
        caseData.getCicCase().setSubjectCIC(set);
        caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        if (!ObjectUtils.isEmpty(dssCaseData.getRepresentativeFullName())) {
            caseData.getCicCase().setRepresentativeFullName(dssCaseData.getRepresentativeFullName());
            caseData.getCicCase().setRepresentativeOrgName(dssCaseData.getRepresentativeOrganisationName());
            caseData.getCicCase().setRepresentativePhoneNumber(dssCaseData.getRepresentativeContactNumber());
            caseData.getCicCase().setRepresentativeEmailAddress(dssCaseData.getRepresentativeEmailAddress());
            caseData.getCicCase().setIsRepresentativeQualified(dssCaseData.getRepresentationQualified());
            caseData.getCicCase().getPartiesCIC().add(PartiesCIC.REPRESENTATIVE);
            final Set<RepresentativeCIC> setRep = new HashSet<>();
            setRep.add(RepresentativeCIC.REPRESENTATIVE);
            caseData.getCicCase().setRepresentativeCIC(setRep);
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        }
        List<CaseworkerCICDocument> docList = new ArrayList<>();
        List<ListValue<DssMessage>> listValues = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dssCaseData.getOtherInfoDocuments())) {
            //For showing additional information page data on tab
            for (ListValue<EdgeCaseDocument> documentListValue : dssCaseData.getOtherInfoDocuments()) {
                Document doc = documentListValue.getValue().getDocumentLink();
                doc.setCategoryId(DocumentType.DSS_OTHER.getCategory());
                CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
                    .documentLink(doc)
                    .documentCategory(DocumentType.DSS_OTHER)
                    .build();
                final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));

                DssMessage message = DssMessage.builder()
                    .message(dssCaseData.getAdditionalInformation())
                    .dateReceived(LocalDate.now())
                    .receivedFrom(caseworkerUser.getUserDetails().getFullName())
                    .documentRelevance(dssCaseData.getDocumentRelevance())
                    .otherInfoDocument(caseworkerCICDocument)
                    .build();

                var listValue = ListValue
                    .<DssMessage>builder()
                    .id("1")
                    .value(message)
                    .build();

                listValues.add(listValue);

            }
        }
        caseData.setMessages(listValues);

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


    private void sendApplicationReceivedNotification(String caseNumber, CaseData data) {

        DssCaseData dssCaseData = data.getDssCaseData();

        if (!dssCaseData.getSubjectFullName().isEmpty()) {
            dssApplicationReceivedNotification.sendToSubject(data, caseNumber);
        }

        if (null != data.getDssCaseData().getRepresentativeFullName()) {
            dssApplicationReceivedNotification.sendToRepresentative(data, caseNumber);
        }
    }


}
