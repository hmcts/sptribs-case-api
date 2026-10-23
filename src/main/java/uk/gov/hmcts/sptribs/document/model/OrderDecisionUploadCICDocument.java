package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.DocumentFileTypes;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Like {@link CICDocument} but solely for uploading orders or decisions.
 * These have additional restrictions on the file type in place (PDF only).
 */
@Data
@NoArgsConstructor
@Builder
public class OrderDecisionUploadCICDocument {

    @CCD(
        label = "Description",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String documentEmailContent;

    @CCD(
        label = "File",
        regex = ".pdf",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document documentLink;

    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public OrderDecisionUploadCICDocument(@JsonProperty("documentEmailContent") String documentEmailContent,
                                          @JsonProperty("documentLink") Document documentLink) {
        this.documentEmailContent = documentEmailContent;
        this.documentLink = documentLink;
    }

    @JsonIgnore
    public boolean isDocumentValid() {
        return DocumentFileTypes.isValid(this.documentLink.getFilename(), DocumentFileTypes.ORDER_DECISION_UPLOAD_EXTENSIONS);
    }
}
