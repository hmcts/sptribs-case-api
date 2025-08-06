package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CollectionDefaultAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseCase {

    @CCD(
        label = "Why is this case being closed?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CloseReason closeCaseReason;

    @CCD(
        label = "Provide additional details",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String additionalDetail;

    @CCD(
        label = "Who withdrew from the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String withdrawalFullName;

    @CCD(
        label = "When was the request to withdraw this case received?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate withdrawalRequestDate;

    @CCD(
        label = "Who made the decision to reject the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList rejectionName;

    @CCD(
        label = "Why was the case rejected?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CloseCaseRejectionReason rejectionReason;

    @CCD(
        label = "Additional details",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String rejectionDetails;

    @CCD(
        label = "When was the case conceded?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate concessionDate;

    @CCD(
        label = "When was the consent order approved?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate consentOrderDate;

    @CCD(
        label = "What was the date of Rule 27 decision?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rule27DecisionDate;

    @CCD(
        label = "Who made the decision to strike out the case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList strikeOutName;

    @CCD(
        label = "Why was the case struck out?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CloseCaseStrikeOutReason strikeOutReason;

    @CCD(
        label = "Additional details",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String strikeOutDetails;

    @CCD(
        label = "Close case documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocument",
        access = {CollectionDefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocument>>  documents;

    @CCD(
        label = "Close case documents",
        typeOverride = Collection,
        typeParameterOverride = "CaseworkerCICDocumentUpload",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CaseworkerCICDocumentUpload>>  documentsUpload;
}
