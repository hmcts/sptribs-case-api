package uk.gov.hmcts.sptribs.common.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Every CCD case type this service registers, one constant apiece.
 *
 * <p>This is the single registry the rest of the service reads: definition loading
 * ({@link HighLevelDataSetupApp}) and local seeding (CftLibConfig) both iterate
 * {@code values()}, so adding a case type is one constant here rather than an
 * edit in several places that can silently disagree.
 */
@AllArgsConstructor
@Getter
public enum CcdServiceCode {

    ST_CIC("ST_CIC", "BBA2", "CRIMINAL_INJURIES_COMPENSATION", CcdCaseType.CIC,
        CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION, "Special Tribunals: Criminal Injuries Compensation"),

    // SEND stays in the ST_CIC jurisdiction. A jurisdiction of its own would need its own
    // CCD roles, an XUI_JURISDICTIONS entry and an IDAM profile row per user.
    ST_SEND35("ST_SEND35", "StSend35", "SEND35", CcdCaseType.SEND35,
        CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION, "Special Tribunals: Special Educational Needs and Disability");

    private final String ccdServiceAcronym;
    @JsonValue
    private final String ccdServiceId;
    private final String ccdServiceName;
    private final CcdCaseType caseType;
    /** The CCD jurisdiction the case type belongs to. */
    private final CcdJurisdiction jurisdiction;
    private final String ccdServiceDescription;
}
