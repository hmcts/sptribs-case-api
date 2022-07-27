package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.ciccase.model.access.CaseworkerAccess;
import uk.gov.hmcts.divorce.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.divorce.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class CicCase {

    @CCD(
        label = "Case Category",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "CaseCategory"
    )
    private CaseCategory caseCategory;

    @CCD(
        label = "Case Subcategory",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
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
        label = "Comment",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String comment;
}
