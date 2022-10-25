package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.CancelHearing;
import uk.gov.hmcts.sptribs.caseworker.model.CaseBuilt;
import uk.gov.hmcts.sptribs.caseworker.model.CaseFlag;
import uk.gov.hmcts.sptribs.caseworker.model.CaseNote;
import uk.gov.hmcts.sptribs.caseworker.model.CaseReinstate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.FlagLevel;
import uk.gov.hmcts.sptribs.caseworker.model.LinkCase;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.caseworker.model.RemoveCaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.access.Applicant2Access;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.SolicitorAndSystemUpdateAccess;
import uk.gov.hmcts.sptribs.payment.model.Payment;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.CasePaymentHistoryViewer;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.FEMALE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.MALE;
import static uk.gov.hmcts.sptribs.ciccase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.sptribs.ciccase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.sptribs.ciccase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.sptribs.payment.model.PaymentStatus.SUCCESS;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @JsonUnwrapped(prefix = "cicCase")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CicCase cicCase = new CicCase();

    @JsonUnwrapped(prefix = "notifications")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private Notifications notifications = new Notifications();

    @CCD(
        label = "Application type",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "ApplicationType"
    )
    private ApplicationType applicationType;

    @CCD(
        label = "Cancel Hearing",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CancelHearing cancelHearing;


    @CCD(
        label = "Flag Location",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagLevel flagLevel;

    @CCD(
        label = "Case Status",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private State caseStatus;

    @CCD(
        label = "Hearing Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;

    @CCD(
        label = "Hearing Location",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingLocation;

    @CCD(
        label = "Divorce or dissolution?",
        access = {DefaultAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "DivorceOrDissolution"
    )
    private DivorceOrDissolution divorceOrDissolution;

    @JsonUnwrapped(prefix = "labelContent")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private LabelContent labelContent = new LabelContent();

    @JsonUnwrapped(prefix = "applicant1")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private Applicant applicant1 = new Applicant();

    @JsonUnwrapped(prefix = "applicant2")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, Applicant2Access.class})
    private Applicant applicant2 = new Applicant();

    @JsonUnwrapped()
    @Builder.Default
    private Application application = new Application();

    @JsonUnwrapped()
    @CCD(access = {DefaultAccess.class})
    private CaseInvite caseInvite;

    @JsonUnwrapped()
    @Builder.Default
    private AcknowledgementOfService acknowledgementOfService = new AcknowledgementOfService();

    @JsonUnwrapped(prefix = "co")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private ConditionalOrder conditionalOrder = new ConditionalOrder();

    @JsonUnwrapped()
    @Builder.Default
    private FinalOrder finalOrder = new FinalOrder();

    @JsonUnwrapped
    @Builder.Default
    private GeneralOrder generalOrder = new GeneralOrder();

    @JsonUnwrapped
    @Builder.Default
    private GeneralEmail generalEmail = new GeneralEmail();

    @JsonUnwrapped
    @Builder.Default
    private GeneralLetter generalLetter = new GeneralLetter();

    @JsonUnwrapped
    @Builder.Default
    private GeneralReferral generalReferral = new GeneralReferral();

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {SolicitorAndSystemUpdateAccess.class})
    private GeneralApplication generalApplication = new GeneralApplication();

    @CCD(
        label = "General Referrals",
        typeOverride = Collection,
        typeParameterOverride = "GeneralReferral"
    )
    private List<ListValue<GeneralReferral>> generalReferrals;

    @CCD(
        label = "Previous Service Applications",
        typeOverride = Collection,
        typeParameterOverride = "AlternativeServiceOutcome",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes;

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {CaseworkerAccessOnlyAccess.class})
    private AlternativeService alternativeService = new AlternativeService();

    @JsonUnwrapped
    @Builder.Default
    private CaseDocuments documents = new CaseDocuments();

    @CCD(
        label = "RDC",
        hint = "Regional divorce unit",
        access = {DefaultAccess.class}
    )
    private Court divorceUnit;

    @CCD(
        label = "General Orders",
        access = {CaseworkerAccessOnlyAccess.class}
    )
    private List<ListValue<DivorceGeneralOrder>> generalOrders;

    @CCD(
        label = "Due Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @CCD(
        label = "Notes",
        typeOverride = Collection,
        typeParameterOverride = "CaseNote",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private List<ListValue<CaseNote>> notes;

    @JsonUnwrapped(prefix = "stay")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseStay caseStay = new CaseStay();

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseBuilt caseBuilt = new CaseBuilt();

    @JsonUnwrapped(prefix = "draft")
    @Builder.Default
    @CCD(
        label = "Select order template ",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DraftOrderCIC draftOrderCIC = new DraftOrderCIC();

    @JsonUnwrapped(prefix = "link")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private LinkCase linkCase = new LinkCase();

    @JsonUnwrapped(prefix = "record")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private RecordListing recordListing = new RecordListing();

    @JsonUnwrapped(prefix = "caseFlag")
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseFlag caseFlag;

    @JsonUnwrapped(prefix = "removeStay")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private RemoveCaseStay removeCaseStay = new RemoveCaseStay();

    @CCD(
        label = "Add a case note",
        hint = "Enter note",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String note;

    @CCD(access = {DefaultAccess.class})
    @JsonUnwrapped
    private RetiredFields retiredFields;


    @CCD(
        label = "Case number",
        access = {CaseworkerAccess.class}
    )
    private String hyphenatedCaseRef;

    @CCD(
        access = {CaseworkerAccess.class}
    )
    @JsonUnwrapped(prefix = "noc")
    private NoticeOfChange noticeOfChange;

    @JsonUnwrapped(prefix = "paperForm")
    @Builder.Default
    @CCD(access = {CaseworkerAccess.class})
    private PaperFormDetails paperFormDetails = new PaperFormDetails();

    @CCD(
        label = "Is case judicial separation?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isJudicialSeparation;

    @CCD(
        label = "General emails",
        typeOverride = Collection,
        typeParameterOverride = "GeneralEmailDetails"
    )
    private List<ListValue<GeneralEmailDetails>> generalEmails;

    @CCD(typeOverride = CasePaymentHistoryViewer)
    private String paymentHistoryField;

    @CCD(
        label = "General letters",
        typeOverride = Collection,
        typeParameterOverride = "GeneralLetterDetails"
    )
    private List<ListValue<GeneralLetterDetails>> generalLetters;
    @CCD(
        label = "Closure Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closureDate;

    @CCD(
        label = "Closed Days",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String closedDayCount;

    @CCD(
        label = "Case Reinstate",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CaseReinstate caseReinstate;

    @JsonIgnore
    public String formatCaseRef(long caseId) {
        String temp = format("%016d", caseId);
        return format("%4s-%4s-%4s-%4s",
            temp.substring(0, 4),
            temp.substring(4, 8),
            temp.substring(8, 12),
            temp.substring(12, 16)
        );
    }

    @JsonIgnore
    public boolean isSoleApplicationOrApplicant2HasAgreedHwf() {
        return nonNull(applicationType)
            && applicationType.isSole()
            || nonNull(application.getApplicant2HelpWithFees())
            && nonNull(application.getApplicant2HelpWithFees().getNeedHelp())
            && application.getApplicant2HelpWithFees().getNeedHelp().toBoolean()
            || FEES_HELP_WITH.equals(application.getSolPaymentHowToPay());
    }

    @JsonIgnore
    public String getApplicant2EmailAddress() {
        final String applicant2Email = applicant2.getEmail();

        if (StringUtils.isEmpty(applicant2Email)) {
            if (nonNull(caseInvite)) {
                return caseInvite.applicant2InviteEmailAddress();
            } else {
                return null;
            }
        }

        return applicant2Email;
    }

    public void archiveAlternativeServiceApplicationOnCompletion() {

        AlternativeService alternativeService = this.getAlternativeService();

        if (null != alternativeService) {

            alternativeService.setReceivedServiceAddedDate(LocalDate.now());

            AlternativeServiceOutcome alternativeServiceOutcome = alternativeService.getOutcome();

            if (isEmpty(this.getAlternativeServiceOutcomes())) {

                List<ListValue<AlternativeServiceOutcome>> listValues = new ArrayList<>();

                var listValue = ListValue
                    .<AlternativeServiceOutcome>builder()
                    .id("1")
                    .value(alternativeServiceOutcome)
                    .build();

                listValues.add(listValue);
                this.setAlternativeServiceOutcomes(listValues);

            } else {

                var listValue = ListValue
                    .<AlternativeServiceOutcome>builder()
                    .value(alternativeServiceOutcome)
                    .build();

                int listValueIndex = 0;
                this.getAlternativeServiceOutcomes().add(0, listValue);
                for (ListValue<AlternativeServiceOutcome> asListValue : this.getAlternativeServiceOutcomes()) {
                    asListValue.setId(String.valueOf(listValueIndex++));
                }
            }
            // Null the current AlternativeService object instance in the CaseData so that a new one can be created
            this.setAlternativeService(null);
        }
    }

    @JsonIgnore
    public boolean isDivorce() {
        return divorceOrDissolution.isDivorce();
    }

    @JsonIgnore
    public void deriveAndPopulateApplicantGenderDetails() {
        Gender app1Gender;
        Gender app2Gender;
        WhoDivorcing whoDivorcing;
        if (this.getDivorceOrDissolution().isDivorce()) {
            // for a divorce we ask who is applicant1 divorcing to infer applicant2's gender, then use the marriage
            // formation to infer applicant 1's gender
            whoDivorcing = this.getApplication().getDivorceWho();
            app2Gender = whoDivorcing == HUSBAND ? MALE : FEMALE;
            app1Gender = this.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app2Gender);
        } else {
            // for a dissolution we ask for applicant1's gender and use the marriage formation to infer applicant 2's
            // gender and who they are divorcing
            app1Gender = this.getApplicant1().getGender();
            app2Gender = this.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app1Gender);
            whoDivorcing = app2Gender == MALE ? HUSBAND : WIFE;
        }

        this.getApplicant1().setGender(app1Gender);
        this.getApplicant2().setGender(app2Gender);
        this.getApplication().setDivorceWho(whoDivorcing);
    }

    @JsonIgnore
    public void updateCaseDataWithPaymentDetails(
        OrderSummary applicationFeeOrderSummary,
        CaseData caseData,
        String paymentReference
    ) {
        var payment = Payment
            .builder()
            .amount(parseInt(applicationFeeOrderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode(applicationFeeOrderSummary.getFees().get(0).getValue().getCode())
            .reference(paymentReference)
            .status(SUCCESS)
            .build();


        var application = caseData.getApplication();

        if (isEmpty(application.getApplicationPayments())) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(new ListValue<>(UUID.randomUUID().toString(), payment));
            application.setApplicationPayments(payments);
        } else {
            application.getApplicationPayments()
                .add(new ListValue<>(UUID.randomUUID().toString(), payment));
        }
    }

    public RemoveCaseStay getRemoveCaseStay() {
        return new RemoveCaseStay();
    }

    public String getClosedDayCount() {
        if (null != closureDate) {
            LocalDate now = LocalDate.now();
            Period period = Period.between(closureDate,now);
            return format("This case has been closed for %d days", period.getDays());
        }
        return "";

    }
}
