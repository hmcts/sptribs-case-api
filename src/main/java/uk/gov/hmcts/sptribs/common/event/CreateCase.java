package uk.gov.hmcts.sptribs.common.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseManagementLocation;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.ApplicantDetails;
import uk.gov.hmcts.sptribs.common.event.page.CaseCategorisationDetails;
import uk.gov.hmcts.sptribs.common.event.page.CaseUploadDocuments;
import uk.gov.hmcts.sptribs.common.event.page.ContactPreferenceDetails;
import uk.gov.hmcts.sptribs.common.event.page.DateOfReceipt;
import uk.gov.hmcts.sptribs.common.event.page.FurtherDetails;
import uk.gov.hmcts.sptribs.common.event.page.RepresentativeDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;
import uk.gov.hmcts.sptribs.common.event.page.SubjectDetails;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.notification.dispatcher.ApplicationReceivedNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateUploadedDocumentCategory;

@Slf4j
@Component
public class CreateCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration categorisationDetails = new CaseCategorisationDetails();
    private static final CcdPageConfiguration dateOfReceipt = new DateOfReceipt();
    private static final CcdPageConfiguration selectParties = new SelectParties();
    private static final CcdPageConfiguration caseUploadDocuments = new CaseUploadDocuments();
    private static final CcdPageConfiguration subjectDetails = new SubjectDetails();
    private static final CcdPageConfiguration applicantDetails = new ApplicantDetails();
    private static final CcdPageConfiguration representativeDetails = new RepresentativeDetails();
    private static final CcdPageConfiguration furtherDetails = new FurtherDetails();
    private static final CcdPageConfiguration contactPreferenceDetails = new ContactPreferenceDetails();

    private final SubmissionService submissionService;

    private final CcdSupplementaryDataService ccdSupplementaryDataService;

    private final ApplicationReceivedNotification applicationReceivedNotification;

    @Autowired
    public CreateCase(SubmissionService submissionService,
                      CcdSupplementaryDataService ccdSupplementaryDataService,
                      ApplicationReceivedNotification applicationReceivedNotification) {
        this.submissionService = submissionService;
        this.ccdSupplementaryDataService = ccdSupplementaryDataService;
        this.applicationReceivedNotification = applicationReceivedNotification;
    }

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_CREATE_CASE)
            .initialState(Draft)
            .name("Create Case")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, SUPER_USER));

        categorisationDetails.addTo(pageBuilder);
        dateOfReceipt.addTo(pageBuilder);
        selectParties.addTo(pageBuilder);
        subjectDetails.addTo(pageBuilder);
        applicantDetails.addTo(pageBuilder);
        representativeDetails.addTo(pageBuilder);
        contactPreferenceDetails.addTo(pageBuilder);
        caseUploadDocuments.addTo(pageBuilder);
        furtherDetails.addTo(pageBuilder);
    }


    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseDetails<CaseData, State> submittedDetails = submissionService.submitApplication(details);
        final CaseData caseData = submittedDetails.getData();

        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = caseData.getCicCase().getCaseDocumentsUpload();
        List<ListValue<CaseworkerCICDocument>> documents = updateUploadedDocumentCategory(uploadedDocuments, false);
        caseData.getCicCase().setCaseDocumentsUpload(new ArrayList<>());

        caseData.getCicCase().setApplicantDocumentsUploaded(documents);
        setIsRepresentativePresent(caseData);
        caseData.setSecurityClass(SecurityClass.PUBLIC);
        caseData.setCaseNameHmctsInternal(caseData.getCicCase().getFullName());

        initialiseFlags(caseData);
        setDefaultCaseDetails(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(submittedDetails.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        setSupplementaryData(details.getId());
        final String caseReference = caseData.getHyphenatedCaseRef();

        try {
            sendApplicationReceivedNotification(caseReference, caseData);
        } catch (Exception notificationException) {
            log.error("Create case notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Create case notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Created %n## Case reference number: %n## %s", caseReference))
            .build();
    }

    private void setDefaultCaseDetails(CaseData data) {
        data.setCaseManagementLocation(
            CaseManagementLocation
                .builder()
                .baseLocation(ST_CIC_WA_CASE_BASE_LOCATION)
                .region(ST_CIC_WA_CASE_REGION)
                .build()
        );

        DynamicListElement caseManagementCategory = new DynamicListElement(
            UUID.randomUUID(), ST_CIC_WA_CASE_MANAGEMENT_CATEGORY);
        data.setCaseManagementCategory(
            DynamicList
                .builder()
                .listItems(List.of(caseManagementCategory))
                .value(caseManagementCategory)
                .build()
        );
        data.setNewBundleOrderEnabled(YesNo.YES);
    }

    private void initialiseFlags(CaseData data) {
        data.setCaseFlags(Flags.builder()
            .details(new ArrayList<>())
            .partyName(null)
            .roleOnCase(null)
            .build());

        if (data.getCicCase().getFullName() != null) {
            data.setSubjectFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getFullName())
                .roleOnCase("subject")
                .build()
            );
        }

        if (data.getCicCase().getApplicantFullName() != null) {
            data.setApplicantFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getApplicantFullName())
                .roleOnCase("applicant")
                .build()
            );
        }

        if (data.getCicCase().getRepresentativeFullName() != null) {
            data.setRepresentativeFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getRepresentativeFullName())
                .roleOnCase("Representative")
                .build()
            );
        }
    }

    private void setSupplementaryData(Long caseId) {
        try {
            ccdSupplementaryDataService.submitSupplementaryDataToCcd(caseId.toString());
        } catch (Exception exception) {
            log.error("Unable to set Supplementary data with exception : {}", exception.getMessage());
        }
    }

    private void sendApplicationReceivedNotification(String caseNumber, CaseData data) {
        final CicCase cicCase = data.getCicCase();

        if (isNotEmpty(cicCase.getSubjectCIC())) {
            applicationReceivedNotification.sendToSubject(data, caseNumber);
        }

        if (isNotEmpty(cicCase.getApplicantCIC())) {
            applicationReceivedNotification.sendToApplicant(data, caseNumber);
        }

        if (isNotEmpty(cicCase.getRepresentativeCIC())) {
            applicationReceivedNotification.sendToRepresentative(data, caseNumber);
        }
    }

    private void setIsRepresentativePresent(CaseData data) {
        data.getCicCase().setIsRepresentativePresent(
            data.getCicCase().getRepresentativeFullName() != null
                ? YesOrNo.YES
                : YesOrNo.NO
        );
    }
}
