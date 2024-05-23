package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.isValidDocument;

@Data
@NoArgsConstructor
@Builder
public class CaseworkerCICDocument {

    @CCD(
        label = "Document Category",
        typeOverride = FixedList,
        typeParameterOverride = "DocumentType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DocumentType documentCategory;

    @CCD(
        label = "Description",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String documentEmailContent;

    @CCD(
        label = "File",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document documentLink;

    @CCD(
        label = "Date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public CaseworkerCICDocument(@JsonProperty("documentCategory") DocumentType documentCategory,
                                 @JsonProperty("documentEmailContent") String documentEmailContent,
                                 @JsonProperty("documentLink") Document documentLink,
                                 @JsonProperty("date") LocalDate date) {
        this.documentCategory = documentCategory;
        this.documentEmailContent = documentEmailContent;
        this.documentLink = documentLink;
        this.date = date;
    }

    @JsonIgnore
    public boolean isDocumentValid() {
        return isValidDocument(this.documentLink.getFilename(), "pdf,csv,txt,rtf,xlsx,docx,doc,xls,mp3,m4a,mp4");
    }

    public boolean isDocumentValid(String validExtensions) {
        return isValidDocument(this.documentLink.getFilename(), validExtensions);
    }

    @JsonIgnore
    public boolean isValidBundleDocument() {
        return isValidDocument(this.documentLink.getFilename(),"pdf,txt,xlsx,docx,doc,xls");
    }
}
