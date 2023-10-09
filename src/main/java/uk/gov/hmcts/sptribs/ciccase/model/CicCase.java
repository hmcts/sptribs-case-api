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
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.HearingCancellationReason;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.caseworker.model.ReminderDays;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.common.model.Status;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
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
        label = "Enter any other important information about this adjournment",
        typeOverride = TextArea
    )
    private String otherDetailsOfAdjournment;

    @CCD(
        label = "What type of decision was given at the hearing?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingOutcome",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingOutcome hearingOutcome;

    @CCD(
        label = "Why was the hearing adjourned?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "AdjournmentReasons",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AdjournmentReasons adjournmentReasons;


    @CCD(
        label = "Enter any other important information about this cancellation",
        typeOverride = TextArea
    )
    private String cancelHearingAdditionalDetail;

    @CCD(
        label = "Why was the hearing cancelled?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingCancellationReason"
    )
    private HearingCancellationReason hearingCancellationReason;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Status status;


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
        label = "How would you like to issue an order?"
    )
    private OrderIssuingType orderIssuingType;

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
    @JsonIgnore
    private Document lastSelectedOrder;

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
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> orderDocumentList;

    @CCD(
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private DynamicList amendDocumentList;

    @CCD(
        label = "Documents",
        typeParameterOverride = "CaseworkerCICDocument",
        access = {DefaultAccess.class}
    )
    private CaseworkerCICDocument selectedDocument;

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
        label = "CCase sub-category",
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
        typeOverride = TextArea
    )
    private String notifyPartyMessage;

    @CCD(
        label = "What is the reason for reinstating the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ReinstateReason reinstateReason;

    @CCD(
        label = "Additional information related to the case reinstatement",
        typeOverride = TextArea
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
        label = "Tribunal name ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String tribunalName = "First-tier Tribunal (CIC)";

    @CCD(
        label = "Tribunal organisation ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String tribunalOrganisation = "Criminal Injuries Compensation";

    @CCD(
        label = "Tribunal email  ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Builder.Default
    private String tribunalEmail = "CIC.enquiries@justice.gov.uk";

    @CCD(
        label = "Subject's full name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fullName;

    @CCD(label = "Subject's address")
    private AddressGlobalUK address;

    @CCD(
        label = "Subject's phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phoneNumber;

    @CCD(
        label = "Subject's email address",
        typeOverride = Email
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
        label = "CICA reference number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String cicaReferenceNumber;

    @CCD(
        label = "Applicant's full name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String applicantFullName;

    @CCD(label = "Applicant's address")
    private AddressGlobalUK applicantAddress;

    @CCD(
        label = "Applicant's phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String applicantPhoneNumber;

    @CCD(
        label = "Applicant's email address",
        typeOverride = Email
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

    @CCD(label = "Representative's Address")
    private AddressGlobalUK representativeAddress;

    @CCD(
        label = "Representative's contact number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String representativePhoneNumber;

    @CCD(
        label = "Representative's email address",
        typeOverride = Email
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

    //new
    @CCD(
        label = "Case Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded;

    @CCD(
        label = "Reinstate Documents",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    List<ListValue<CaseworkerCICDocument>> reinstateDocuments;


    @CCD(
        label = "Decision Documents",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> decisionDocumentList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> removedDocumentList;

    @CCD(
        label = "Final Decision Documents",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> finalDecisionDocumentList;


    private YesOrNo selectedCheckBox;

    @CCD(
        label = "Case Status",
        typeOverride = FixedRadioList,
        typeParameterOverride = "State",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private State testState;

    @CCD(
        label = "Minus days from today to set close date ",
        regex = "^\\d+$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String days;


    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnore
    private NotificationResponse subjectNotifyList;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationResponse appNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnore
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
    private String firstDueDate;


    @CCD(
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String eventName;

    private LocalDate findEarliestDate(List<ListValue<DateModel>> dueDateList, LocalDate compare) {
        LocalDate earliestDate = compare;
        for (ListValue<DateModel> dateModelListValue : dueDateList) {
            if ((null == dateModelListValue.getValue().getOrderMarkAsCompleted()
                || !dateModelListValue.getValue().getOrderMarkAsCompleted().contains(GetAmendDateAsCompleted.MARKASCOMPLETED))
                && dateModelListValue.getValue().getDueDate().isBefore(compare)) {
                earliestDate = dateModelListValue.getValue().getDueDate();
            }
        }
        return earliestDate;

    }

    public String getFirstDueDate() {

        DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
        LocalDate compare = LocalDate.MAX;
        if (!CollectionUtils.isEmpty(orderList)) {
            for (ListValue<Order> orderListValue : orderList) {
                if (!CollectionUtils.isEmpty(orderListValue.getValue().getDueDateList())) {
                    compare = findEarliestDate(orderListValue.getValue().getDueDateList(), compare);
                }
            }
            if (compare.isBefore(LocalDate.MAX)) {
                return dateFormatter.format(compare);
            }
        }
        return "";
    }

    @JsonIgnore
    public String getSelectedHearingToCancel() {
        return this.getHearingList() != null ? this.getHearingList().getValue().getLabel() : null;
    }

    public void removeRepresentative() {
        if (null != representativeCIC) {
            representativeCIC = new HashSet<>();
        }
        if (null != notifyPartyRepresentative) {
            notifyPartyRepresentative = new HashSet<>();
        }
        if (null != hearingNotificationParties) {
            hearingNotificationParties.remove(NotificationParties.REPRESENTATIVE);
        }
        if (null != contactPartiesCIC) {
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
        if (null != applicantCIC) {
            applicantCIC = new HashSet<>();
        }
        if (null != notifyPartyApplicant) {
            notifyPartyApplicant = new HashSet<>();
        }
        if (null != hearingNotificationParties) {
            hearingNotificationParties.remove(NotificationParties.APPLICANT);
        }
        applicantFullName = "";
        applicantAddress = new AddressGlobalUK();
        applicantPhoneNumber = "";
        applicantEmailAddress = "";

    }
}
