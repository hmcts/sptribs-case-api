package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ComponentLauncher;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.CaseBuilt;
import uk.gov.hmcts.sptribs.caseworker.model.CaseFlag;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseNote;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.EditCicaCaseDetails;
import uk.gov.hmcts.sptribs.caseworker.model.FlagLevel;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.caseworker.model.RemoveCaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.bundling.Bundle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    @JsonUnwrapped
    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DocumentManagement docManagement = new DocumentManagement();

    @Builder.Default
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
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
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<Bundle>> cicBundles = new ArrayList<>();

    @JsonUnwrapped(prefix = "cicCase")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CicCase cicCase = new CicCase();

    @JsonUnwrapped(prefix = "notifications")
    @Builder.Default
    @CCD(access = {DefaultAccess.class})
    private Notifications notifications = new Notifications();

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


    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Listing listing = new Listing();

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

    @CCD(
        label = "Case number",
        access = {CaseworkerAccess.class}
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
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
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

    @JsonUnwrapped(prefix = "issueCase")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseIssue caseIssue = new CaseIssue();

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
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private DssCaseData dssCaseData = new DssCaseData();

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
