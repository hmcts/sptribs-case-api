package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@Builder
public class CaseworkerCICDocument {

    @CCD(
        label = "Document Category"
    )
    private DocumentType documentCategory;

    @CCD(
        label = "Description",
        typeOverride = TextArea
    )
    private String documentEmailContent;

    @CCD(
        label = "File",
        regex = ".pdf,.tif,.tiff,.jpg,.jpeg,.png,.mp3"
    )
    private Document documentLink;


    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public CaseworkerCICDocument(@JsonProperty("documentCategory") DocumentType documentCategory,
                                 @JsonProperty("documentEmailContent") String documentEmailContent,
                                 @JsonProperty("documentLink") Document documentLink) {
        this.documentCategory = documentCategory;
        this.documentEmailContent = documentEmailContent;
        this.documentLink = documentLink;
    }
}
