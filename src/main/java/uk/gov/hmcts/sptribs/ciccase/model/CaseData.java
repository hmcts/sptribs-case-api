package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.External;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ComponentLauncher;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.FlagLauncher;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.SearchCriteria;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.CaseBuilt;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseManagementLocation;
import uk.gov.hmcts.sptribs.caseworker.model.CaseNote;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.EditCicaCaseDetails;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.caseworker.model.RemoveCaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseFileViewAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseFlagsAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseLinksDefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerRASValidationAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CollectionCreateUpdateOnlyAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DSSUpdateAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.GlobalSearchAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.NonRespondentAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.SuperUserOnlyAccess;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleIdAndTimestamp;
import uk.gov.hmcts.sptribs.document.bundling.model.MultiBundleConfig;
import uk.gov.hmcts.sptribs.document.model.AbstractCaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @CCD(access = {CaseLinksDefaultAccess.class},
        typeOverride = Collection,
        label = "Linked Cases",
        typeParameterOverride = "CaseLink")
    @Builder.Default
    private List<ListValue<CaseLink>> caseLinks = new ArrayList<>();

    @CCD(
        label = "Component Launcher (for displaying Linked Cases data)",
        access = {CaseLinksDefaultAccess.class}
    )
    @JsonProperty("LinkedCasesComponentLauncher")
    private ComponentLauncher linkedCasesComponentLauncher;

    @CCD(
        label = "Launch the Flags screen",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagLauncher flagLauncher;

    @CCD(
        label = "Case name Hmcts Internal",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, GlobalSearchAccess.class}
    )
    private String caseNameHmctsInternal;

    @CCD(
        label = "Search Criteria",
        access = {GlobalSearchAccess.class}
    )
    @SuppressWarnings("MemberName") // Field name is case-sensitive in CCD
    @JsonProperty("SearchCriteria")
    private SearchCriteria SearchCriteria;

    @CCD(
        label = "Case Location",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class,
            GlobalSearchAccess.class, CaseworkerRASValidationAccess.class}
    )
    private CaseManagementLocation caseManagementLocation;

    @CCD(
        label = "Case Management Category",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class, GlobalSearchAccess.class}
    )
    private DynamicList caseManagementCategory;

    @CCD(access = {DefaultAccess.class, CaseFlagsAccess.class},
        label = "Case Flags")
    private Flags caseFlags;

    @CCD(access = {DefaultAccess.class, CaseFlagsAccess.class},
        label = "Flags for Subject")
    private Flags subjectFlags;

    @CCD(access = {DefaultAccess.class, CaseFlagsAccess.class},
        label = "Flags for Representative")
    private Flags representativeFlags;

    @CCD(access = {DefaultAccess.class, CaseFlagsAccess.class},
        label = "Flags for Applicant")
    private Flags applicantFlags;

    @JsonUnwrapped(prefix = "all")
    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DocumentManagement allDocManagement = new DocumentManagement();

    @JsonUnwrapped(prefix = "new")
    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DocumentManagement newDocManagement = new DocumentManagement();

    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "CICA Case Details"
    )
    private EditCicaCaseDetails editCicaCaseDetails = new EditCicaCaseDetails();

    @JsonUnwrapped(prefix = "orderContent")
    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DraftOrderContentCIC draftOrderContentCIC = new DraftOrderContentCIC();

    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private ContactParties contactParties = new ContactParties();

    @CCD(
        label = "Change security classification",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "SecurityClass")
    private SecurityClass securityClass;

    @Builder.Default
    @CCD(
        label = "Bundles",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<Bundle>> caseBundles = new ArrayList<>();

    @Builder.Default
    @CCD(
        label = "Bundles",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<BundleIdAndTimestamp>> caseBundleIdsAndTimestamps = new ArrayList<>();

    @JsonUnwrapped(prefix = "cicCase")
    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CicCase cicCase = new CicCase();

    @JsonUnwrapped(prefix = "notifications")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private Notifications notifications = new Notifications();

    @CCD(
        label = "Case Status",
        typeOverride = FixedList,
        typeParameterOverride = "State",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private State caseStatus;

    @CCD(
        label = "CCD Case Reference",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String caseNumber;

    @CCD(
        label = "SubjectRepFullName",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String subjectRepFullName;

    @CCD(
        label = "Scheme Label",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String schemeLabel;

    @CCD(
        label = "Bundle Configuration",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private MultiBundleConfig bundleConfiguration;

    @CCD(
        label = "Multi Bundle Configuration",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<MultiBundleConfig> multiBundleConfiguration;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> caseDocuments; // bundle field

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> furtherCaseDocuments; // bundle field

    @CCD(
        label = "Initial CICA Documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {CollectionCreateUpdateOnlyAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> initialCicaDocuments;

    @CCD(
        label = "Further Document Uploads",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {CollectionCreateUpdateOnlyAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>> furtherUploadedDocuments;

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
        label = "Due Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @CCD(
        label = "Notes",
        typeOverride = Collection,
        typeParameterOverride = "CaseNote",
        access = {NonRespondentAccess.class}
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

    @JsonUnwrapped
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    @Builder.Default
    private Listing listing = new Listing();

    @JsonUnwrapped(prefix = "nh")
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    @Builder.Default
    private Listing nextListedHearing = new Listing();

    @JsonUnwrapped(prefix = "lh")
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    @Builder.Default
    private Listing latestCompletedHearing = new Listing();

    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Listings",
        typeOverride = Collection,
        typeParameterOverride = "Listing")
    private List<ListValue<Listing>> hearingList = new ArrayList<>();

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

    @CCD(
        label = "Correspondence",
        typeOverride = Collection,
        typeParameterOverride = "Correspondence",
        access = {NonRespondentAccess.class}
    )
    @External
    private List<ListValue<Correspondence>> correspondence;

    @CCD(
        label = "Case number",
        access = {DefaultAccess.class, CaseworkerAccess.class}
    )
    private String hyphenatedCaseRef;

    @CCD(
        label = "Is case judicial separation?",
        access = {DefaultAccess.class}
    )
    private YesOrNo isJudicialSeparation;

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
        label = "Case file view",
        access = {CaseFileViewAccess.class}
    )
    private ComponentLauncher caseFileView1;

    @CCD(
        label = "Event",
        access = {DefaultAccess.class}
    )
    private String currentEvent;

    @CCD(
        label = "Decision notice signature",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String decisionSignature;

    @CCD(
        access = {CaseworkerAndSuperUserAccess.class},
        typeOverride = TextArea
    )
    private String decisionMainContent;

    @CCD(
        label = "Messages",
        typeOverride = Collection,
        typeParameterOverride = "DssMessage",
        access = {DefaultAccess.class, CaseworkerAndSuperUserAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private List<ListValue<DssMessage>> messages;

    @JsonUnwrapped(prefix = "issueCase")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseIssue caseIssue = new CaseIssue();

    @JsonUnwrapped(prefix = "contactPartiesDocuments")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();

    @JsonUnwrapped(prefix = "caseIssueDecision")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseIssueDecision caseIssueDecision = new CaseIssueDecision();

    @JsonUnwrapped(prefix = "caseIssueFinalDecision")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseIssueFinalDecision caseIssueFinalDecision = new CaseIssueFinalDecision();

    @JsonUnwrapped(prefix = "close")
    @Builder.Default
    @CCD(
        label = "Close Case",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CloseCase closeCase = new CloseCase();

    @JsonUnwrapped(prefix = "referToJudge")
    @Builder.Default
    @CCD(
        label = "Why are you referring the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private ReferToJudge referToJudge = new ReferToJudge();

    @JsonUnwrapped(prefix = "referToLegalOfficer")
    @Builder.Default
    @CCD(
        label = "Why are you referring the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private ReferToLegalOfficer referToLegalOfficer = new ReferToLegalOfficer();

    @JsonUnwrapped(prefix = "dssCaseData")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class})
    private DssCaseData dssCaseData = new DssCaseData();

    @CCD(
        label = "PCQ ID",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private String pcqId;

    @CCD(
        access = { DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssQuestion1;

    @CCD(
        access = { DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssAnswer1;

    @CCD(
        access = { DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssQuestion2;

    @CCD(
        access = {DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssAnswer2;

    @CCD(
        access = {DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssQuestion3;

    @CCD(
        access = { DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssAnswer3;

    @CCD(
        label = "Uploaded DSS Documents",
        typeOverride = Collection,
        typeParameterOverride = "DssUploadedDocument",
        access = {CaseworkerAccess.class, DSSUpdateAccess.class}
    )
    private List<ListValue<DssUploadedDocument>> uploadedDssDocuments;

    @CCD(
        access = { DefaultAccess.class, DSSUpdateAccess.class}
    )
    private String dssHeaderDetails;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private YesOrNo hasDssNotificationSent;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private String firstHearingDate;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private String hearingVenueName;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private String judicialId;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private YesNo stitchHearingBundleTask;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private YesNo completeHearingOutcomeTask;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class,
        GlobalSearchAccess.class, CaseworkerRASValidationAccess.class})
    private YesNo newBundleOrderEnabled;

    @CCD(access = {DefaultAccess.class})
    @JsonUnwrapped
    private RetiredFields retiredFields;

    @CCD(
        label = "Do you want to delete a field from case data?",
        access = {SuperUserOnlyAccess.class}
    )
    private YesNo deleteField;

    @CCD(
        label = "Enter the name of the field to delete",
        hint = "Enter the exact field name as it appears in the case data (e.g., 'hyphenatedCaseRef', 'cicCaseFullName')",
        access = {SuperUserOnlyAccess.class}
    )
    private String deleteFieldName;

    @CCD(
        label = "Reindex cases modified since",
        access = {SuperUserOnlyAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    @External
    private LocalDate reindexCasesModifiedSince;

    @CCD(
        label = "Matching cases count",
        access = {SuperUserOnlyAccess.class}
    )
    @External
    private Long reindexCasesMatchingCount;

    public String getFirstHearingDate() {

        Listing nextListing = getNextListedHearing();
        DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
        if (!ObjectUtils.isEmpty(nextListing) && !ObjectUtils.isEmpty(nextListing.getDate())) {
            return dateFormatter.format(nextListing.getDate());
        }
        return "";

    }

    @JsonIgnore
    public boolean isBundleOrderEnabled() {
        return YesNo.YES.equals(this.newBundleOrderEnabled);
    }

    @JsonIgnore
    public Listing getLatestCompletedHearing() {

        Listing completedHearing = new Listing();
        LocalDate latest = LocalDate.MIN;
        for (ListValue<Listing> listingValueList : hearingList) {
            if (listingValueList.getValue().getDate().isAfter(latest)) {
                latest = listingValueList.getValue().getDate();
                completedHearing = listingValueList.getValue();
            }
        }

        return completedHearing;
    }

    @JsonIgnore
    public Listing getNextListedHearing() {
        Listing nextListing = new Listing();

        LocalDate compare = LocalDate.MAX;
        if (!CollectionUtils.isEmpty(hearingList)) {
            for (ListValue<Listing> listingValueList : hearingList) {
                if (listingValueList.getValue().getHearingStatus() == HearingState.Listed
                    && listingValueList.getValue().getDate().isBefore(compare)) {
                    compare = listingValueList.getValue().getDate();
                    nextListing = listingValueList.getValue();
                }
            }
        }
        return nextListing;
    }

    public String getHearingVenueName() {
        Listing nextListing = getNextListedHearing();
        if (!ObjectUtils.isEmpty(nextListing)) {
            return nextListing.getHearingVenueNameAndAddress();
        }
        return "";
    }

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

    public String getClosedDayCount() {
        if (null != closureDate) {
            LocalDate now = LocalDate.now();
            long days = ChronoUnit.DAYS.between(closureDate, now);
            return format("This case has been closed for %d days", days);
        }
        return "";

    }
}
