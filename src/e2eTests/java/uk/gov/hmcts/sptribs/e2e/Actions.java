package uk.gov.hmcts.sptribs.e2e;

public enum Actions {
    AddNote("Case: Add note"),
    ContactParties("Case: Contact parties"),
    EdisCase("Case: Edit case"),
    CreateFlag("Flags: Create flag"),
    ManageFlags("Flags: Manage flags"),
    CancelHearing("Hearings: Cancel hearing"),
    CreateListing("Hearings: Create listing"),
    CreateSummary("Hearings: Create summary"),
    EditListing("Hearings: Edit listing"),
    PostponeHearing("Hearings: Postpone hearing"),
    LinkCase("Links: Link case"),
    CreateDraft("Orders: Create draft"),
    EditeDraft("Orders: Edit draft"),
    ManageDueDate("Orders: Manage due date"),
    SendOrder("Orders: Send order"),
    TestChangeState("Test change state");

    public final String label;

    Actions(String label) {
        this.label = label;
    }
}
