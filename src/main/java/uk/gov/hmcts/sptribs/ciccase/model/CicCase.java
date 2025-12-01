package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.CreateAndSendIssuingType;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.caseworker.model.ReminderDays;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CollectionDefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class CicCase {
    @CCD(
        access = {DefaultAccess.class}
    )
    private String referralTypeForWA;

    @CCD(
        label = "Preview order",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        categoryID = "TD"
    )
    private Document orderTemplateIssued;

    @CCD(
        label = "Order to be sent",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList draftOrderDynamicList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList flagDynamicList;

    @CCD(
        label = "Template",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList orderDynamicList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList hearingList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList hearingSummaryList;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "ContactPartiesCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ContactPartiesCIC> contactPartiesCIC;

    @CCD(
            label = "Apply anonymity to the case?",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo anonymiseYesOrNo;

    @CCD(
            label = "Anonymised Name",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String anonymisedAppellantName;

    @CCD(
        label = "How would you like to issue an order?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private OrderIssuingType orderIssuingType;

    @CCD(
        label = "How would you like to issue an order?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CreateAndSendIssuingType createAndSendIssuingTypes;

    @CCD(
        label = "Draft order",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<DraftOrderCIC>> draftOrderCICList;

    @CCD(
        label = "Due Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<DateModel>> orderDueDates;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document selectedOrder;

    @CCD(
        label = "Should a reminder notification be sent? You can only send a reminder for the earliest due date stated on this order",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesNo orderReminderYesOrNo;

    @CCD(
        label = "How many days before the earliest due date should a reminder be sent?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ReminderDays orderReminderDays;

    @CCD(
        label = "Sent Order",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<Order>> orderList;

    @CCD(
        label = "Order Documents",
        access = {CollectionDefaultAccess.class, CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> orderDocumentList;

    @CCD(
        label = "Amended Document",
        hint = "Please select a document from the dropdown menu",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private DynamicList amendDocumentList;

    @CCD(
        label = "Document Category",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DocumentType selectedDocumentCategory;

    @CCD(
        label = "Description",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String selectedDocumentEmailContent;

    @CCD(
        label = "File",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document selectedDocumentLink;

    @CCD(
        label = "Notified Parties",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<NotificationParties> hearingNotificationParties;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String selectedDocumentType;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo isDocumentCreatedFromTemplate;

    @CCD(
        label = "Upload a file to the system",
        typeOverride = Collection,
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CICDocument>> orderFile;

    @CCD(
        label = "Case category",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "CaseCategory"
    )
    private CaseCategory caseCategory;

    @CCD(
        label = "Case sub-category",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "CaseSubcategory"
    )
    private CaseSubcategory caseSubcategory;

    @CCD(
        label = "Case Received Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate caseReceivedDate;

    @CCD(
        label = "Is Tribunal Application On Time?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isTribunalApplicationOnTime;

    @CCD(
        label = "Is Late Tribunal Application Reason Given?",
        access = {CaseworkerAccess.class}
    )
    private YesOrNo isLateTribunalApplicationReasonGiven;

    @CCD(
        label = "Named Parties",
        typeOverride = MultiSelectList,
        typeParameterOverride = "PartiesCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<PartiesCIC> partiesCIC;

    @CCD(
        label = "Case information recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "SubjectCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<SubjectCIC> subjectCIC;

    @CCD(
        label = "Case information recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicantCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ApplicantCIC> applicantCIC;

    @CCD(
        label = "Case information recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "RepresentativeCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<RepresentativeCIC> representativeCIC;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "ApplicantCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ApplicantCIC> notifyPartyApplicant;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "SubjectCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<SubjectCIC> notifyPartySubject;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "RepresentativeCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<RepresentativeCIC> notifyPartyRepresentative;

    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "RespondentCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<RespondentCIC> notifyPartyRespondent;

    @CCD(
        label = "Message",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String notifyPartyMessage;

    @CCD(
        label = "What is the reason for reinstating the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ReinstateReason reinstateReason;

    @CCD(
        label = "Additional information related to the case reinstatement",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String reinstateAdditionalDetail;

    @CCD(
        label = "Respondent name ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String respondentName = "Appeals team";

    @CCD(
        label = "Respondent organisation ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String respondentOrganisation = "CICA";

    @CCD(
        label = "Respondent email  ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String respondentEmail = "appeals.team@cica.gov.uk";

    @CCD(
        label = "Subject's full name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fullName;

    @CCD(
        label = "Subject's address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK address;

    @CCD(
        label = "Subject's phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String phoneNumber;

    @CCD(
        label = "Subject's email address",
        typeOverride = Email,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String email;

    @CCD(
        label = "Subject's date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @CCD(
        label = "What is subject's contact preference type?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType contactPreferenceType;

    @CCD(
        label = "Scheme",
        typeOverride = FixedList,
        typeParameterOverride = "SchemeCic",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private SchemeCic schemeCic;

    @CCD(
        label = "Case Region",
        typeOverride = FixedList,
        typeParameterOverride = "RegionCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private RegionCIC regionCIC;

    @CCD(
        label = "Linked CICA reference number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String cicaReferenceNumber;

    @CCD(
        label = "Applicant's full name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String applicantFullName;

    @CCD(
        label = "Applicant's address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK applicantAddress;

    @CCD(
        label = "Applicant's phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String applicantPhoneNumber;

    @CCD(
        label = "Applicant's email address",
        typeOverride = Email,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String applicantEmailAddress;

    @CCD(
        label = "Applicant's date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicantDateOfBirth;

    @CCD(
        label = "What is applicant's contact preference?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType applicantContactDetailsPreference;

    @CCD(
        label = "Representative's full name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeFullName;

    @CCD(
        label = "Organisation or business name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeOrgName;

    @CCD(
        label = "Representative's reference",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeReference;

    @CCD(
        label = "Representative's Address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK representativeAddress;

    @CCD(
        label = "Representative's contact number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativePhoneNumber;

    @CCD(
        label = "Representative's email address",
        typeOverride = Email,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String representativeEmailAddress;

    @CCD(
        label = "Representative's date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate representativeDateOfBirth;

    @CCD(
        label = "What is representative's contact preference?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ContactPreferenceType representativeContactDetailsPreference;

    @CCD(
        label = "Is the representative legally qualified?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo isRepresentativeQualified;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo representativeDetailsObjects;

    @CCD(
        label = "Have the tribunal forms been received in time?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo formReceivedInTime;

    @CCD(
        label = "Has the applicant explained why they missed the deadline?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo missedTheDeadLineCic;

    @CCD(
        label = "Have any claims linked to this case been lodged with CICA? ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo claimLinkedToCic;

    @CCD(
        label = "Are there any ongoing compensation claims linked to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo compensationClaimLinkCIC;

    @CCD(
        label = "Case Number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String caseNumber;

    @CCD(
        label = "Is there a representative?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo isRepresentativePresent;

    @CCD(
        label = "Case Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {CollectionDefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded;

    @CCD(
        label = "Case Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocumentUpload",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocumentUpload>> caseDocumentsUpload;

    @CCD(
        label = "Reinstate Documents",
        access = {CollectionDefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    List<ListValue<CaseworkerCICDocument>> reinstateDocuments;

    @CCD(
        label = "Reinstate Documents",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    List<ListValue<CaseworkerCICDocumentUpload>> reinstateDocumentsUpload;

    @CCD(
        label = "Decision Documents",
        access = {CollectionDefaultAccess.class, CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> decisionDocumentList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> removedDocumentList;

    @CCD(
        label = "Final Decision Documents",
        access = {CollectionDefaultAccess.class, CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> finalDecisionDocumentList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo selectedCheckBox;

    @CCD(
        label = "Minus days from today to set close date ",
        regex = "^\\d+$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String days;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse subjectNotifyList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse appNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse repNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse tribunalNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse resNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse subjectLetterNotifyList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse appLetterNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse subHearingNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse repHearingNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse resHearingNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse repLetterNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstOrderDueDate;

    private LocalDate findEarliestDate(List<ListValue<DateModel>> dueDateList, LocalDate compare) {
        LocalDate earliestDate = compare;
        for (ListValue<DateModel> dateModelListValue : dueDateList) {
            if ((dateModelListValue.getValue().getOrderMarkAsCompleted() == null
                || !dateModelListValue.getValue().getOrderMarkAsCompleted().contains(GetAmendDateAsCompleted.MARKASCOMPLETED))
                && dateModelListValue.getValue().getDueDate().isBefore(compare)) {
                earliestDate = dateModelListValue.getValue().getDueDate();
            }
        }
        return earliestDate;
    }

    public LocalDate calculateFirstDueDate() {
        LocalDate compare = LocalDate.MAX;

        if (!CollectionUtils.isEmpty(orderList)) {
            for (ListValue<Order> orderListValue : orderList) {
                if (!CollectionUtils.isEmpty(orderListValue.getValue().getDueDateList())) {
                    compare = findEarliestDate(orderListValue.getValue().getDueDateList(), compare);
                }
            }

            if (compare.isBefore(LocalDate.MAX)) {
                return compare;
            }
        }

        return null;
    }

    @JsonIgnore
    public String getSelectedHearingToCancel() {
        return this.getHearingList() != null ? this.getHearingList().getValue().getLabel() : null;
    }

    public void removeRepresentative() {
        if (representativeCIC != null) {
            representativeCIC = new HashSet<>();
        }
        if (notifyPartyRepresentative != null) {
            notifyPartyRepresentative = new HashSet<>();
        }
        if (hearingNotificationParties != null) {
            hearingNotificationParties.remove(NotificationParties.REPRESENTATIVE);
        }

        if (contactPartiesCIC != null) {
            Set<ContactPartiesCIC> temp = new HashSet<>();
            for (ContactPartiesCIC partyCIC : contactPartiesCIC) {
                if (partyCIC != ContactPartiesCIC.REPRESENTATIVETOCONTACT) {
                    temp.add(partyCIC);
                }
            }
            contactPartiesCIC = temp;
        }

        representativeFullName = "";
        representativeOrgName = "";
        representativeReference = "";
        representativeAddress = new AddressGlobalUK();
        representativePhoneNumber = "";
        representativeEmailAddress = "";
    }

    public void removeApplicant() {
        if (applicantCIC != null) {
            applicantCIC = new HashSet<>();
        }
        if (notifyPartyApplicant != null) {
            notifyPartyApplicant = new HashSet<>();
        }
        if (hearingNotificationParties != null) {
            hearingNotificationParties.remove(NotificationParties.APPLICANT);
        }

        applicantFullName = "";
        applicantAddress = new AddressGlobalUK();
        applicantPhoneNumber = "";
        applicantEmailAddress = "";
    }

    public boolean useApplicantNameForSubject() {
        return (caseSubcategory == CaseSubcategory.FATAL
            || caseSubcategory == CaseSubcategory.MINOR)
            && (applicantFullName != null);
    }
}
