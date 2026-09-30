package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.DynamicMultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactPartiesDocuments {

    @CCD(label = "Document 1", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d01;

    @CCD(label = "Document 2", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d02;

    @CCD(label = "Document 3", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d03;

    @CCD(label = "Document 4", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d04;

    @CCD(label = "Document 5", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d05;

    @CCD(label = "Document 6", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d06;

    @CCD(label = "Document 7", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d07;

    @CCD(label = "Document 8", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d08;

    @CCD(label = "Document 9", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d09;

    @CCD(label = "Document 10", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Document d10;

    @CCD(label = "Selected parties", typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private String reviewSelectedParties;

    @CCD(label = "Message", typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private String reviewMessage;

    @CCD(typeOverride = DynamicMultiSelectList,
        typeParameterOverride = "DynamicList",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    private DynamicMultiSelectList documentList;

}
