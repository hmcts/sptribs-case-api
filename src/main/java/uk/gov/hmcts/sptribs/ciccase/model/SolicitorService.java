package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class SolicitorService {

    @CCD(
        label = "Date of Service",
        typeOverride = Date,
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfService;

    @CCD(
        label = "What Documents did you serve?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String documentsServed;

    @CCD(
        label = "On whom did you serve?",
        hint = "(if appropriate include their position e.g. partner, director)",
        access = {DefaultAccess.class}
    )
    private String onWhomServed;

    @CCD(
        label = "Details of Service",
        access = {DefaultAccess.class}
    )
    private String serviceDetails;

    @CCD(
        label = "Give the address where you served the documents?",
        hint = "Include the fax or DX number, e-mail address or other electronic identification",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String addressServed;

    @CCD(
        label = "Specify Location",
        access = {DefaultAccess.class}
    )
    private String specifyLocationServed;

    @CCD(
        label = "Solicitor’s Name",
        access = {DefaultAccess.class}
    )
    private String serviceSotName;

    @CCD(
        label = "Solicitor’s Firm",
        access = {DefaultAccess.class}
    )
    private String serviceSotFirm;

    @CCD(
        label = " ",
        access = {DefaultAccess.class}
    )
    private String truthStatement;

    public String getTruthStatement() {
        return "I believe that the facts stated in the application are true.";
    }
}
