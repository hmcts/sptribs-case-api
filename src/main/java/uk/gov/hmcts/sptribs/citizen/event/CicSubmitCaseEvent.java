package uk.gov.hmcts.sptribs.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Submitted;
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
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@Setter
public class CicSubmitCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.dss-frontend.enabled}")
    private boolean dssSubmitCaseEnabled;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    AppsConfig appsConfig;

    @Value("${feature.sni-5511.enabled}")
    private boolean sni5511Enabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (dssSubmitCaseEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                .getSubmitEvent())
            .forStateTransition(DSS_Draft, DSS_Submitted)
            .name("Submit case (cic)")
            .description("Application submit (cic)")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE_DELETE, CITIZEN)
            .grant(CREATE_READ_UPDATE, SYSTEM_UPDATE, CREATOR)
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

        final CaseData data = details.getData();
        final DssCaseData dssData = details.getData().getDssCaseData();
        final CaseData caseData = getCaseData(data, dssData);

        setDssMetaData(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void setDssMetaData(CaseData data) {
        data.setDssQuestion1("Full Name");
        data.setDssQuestion2("Date of Birth");
        data.setDssAnswer1("case_data.dssCaseDataSubjectFullName");
        data.setDssAnswer2("case_data.dssCaseDataSubjectDateOfBirth");
        data.setDssHeaderDetails("Subject of this case");
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

                if (sni5511Enabled && !docList.contains(caseworkerCICDocument)) {
                    docList.add(caseworkerCICDocument);
                }

                final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));

                final DssMessage message = DssMessage.builder()
                    .message(dssCaseData.getAdditionalInformation())
                    .dateReceived(LocalDate.now())
                    .receivedFrom(caseworkerUser.getUserDetails().getFullName())
                    .documentRelevance(dssCaseData.getDocumentRelevance())
                    .build();

                if (!sni5511Enabled) {
                    message.setOtherInfoDocument(caseworkerCICDocument);
                }

                final ListValue<DssMessage> listValue = ListValue
                    .<DssMessage>builder()
                    .id(UUID.randomUUID().toString())
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


}
