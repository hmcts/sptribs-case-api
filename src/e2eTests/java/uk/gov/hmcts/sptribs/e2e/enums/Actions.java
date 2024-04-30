package uk.gov.hmcts.sptribs.e2e.enums;

public enum Actions {
    AddNote("Case: Add note"),
    ContactParties("Case: Contact parties"),
    EditCase("Case: Edit case"),
    CreateABundle("Bundle: Create a bundle"),
    CreateFlag("Create Flag"),
    ManageFlags("Manage Flags"),
    CancelHearing("Hearings: Cancel hearing"),
    CreateListing("Hearings: Create listing"),
    CreateSummary("Hearings:Create summary"),
    EditCicaCaseDetails("Case: Edit case details"),
    EditListing("Hearings: Edit listing"),
    EditSummary("Hearings: Edit summary"),
    PostponeHearing("Hearings: Postpone hearing"),
    LinkCases("Link cases"),
    ManageCaseLinks("Manage case links"),
    CreateDraft("Orders: Create draft"),
    EditeDraft("Orders: Edit draft"),
    ManageDueDate("Orders: Manage due date"),
    SendOrder("Orders: Send order"),
    CloseCase("Case: Close case"),
    BuildCase("Case: Build case"),
    CreateEditStay("Stays: Create/edit stay"),
    ReinstateCase("Case: Reinstate case"),
    IssueDecision("Decision: Issue a decision"),
    IssueFinalDecision("Decision: Issue final decision"),
    IssueCaseToRespondent("Case: Issue to respondent"),
    RemoveStay("Stays: Remove stay"),
    TestChangeState("Test change state"),
    ReferCaseToJudge("Refer case to judge"),
    ReferCaseToLegalOfficer("Refer case to legal officer"),
    UploadDocuments("Document management: Upload"),
    AmendDocuments("Document management: Amend"),
    RemoveDocuments("Document management: Remove");

    public final String label;

    Actions(String label) {
        this.label = label;
    }
}
