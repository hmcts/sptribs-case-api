package uk.gov.hmcts.sptribs.e2e.enums;

public enum DraftOrderTemplate {
    CIC6GeneralDirections("CIC6 - General Directions"),
    CIC7MEDmiReports("CIC7 - ME Dmi Reports"),
    CIC8MEJointInstruction("CIC8 - ME Joint Instruction"),
    CIC10StrikeOutWarning("CIC10 - Strike Out Warning"),
    CIC13ProFormaSummons("CIC13 - Pro Forma Summons"),
    CIC14LOGeneralDirections("CIC14 - LO General Directions");

    public final String label;

    DraftOrderTemplate(String label) {
        this.label = label;
    }
}
