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
public class CICDocument {

    @CCD(
        label = "Description",
        typeOverride = TextArea
    )
    private String documentEmailContent;

    @CCD(
        label = "File",
        regex = ".pdf,.tif,.tiff,.jpg,.jpeg,.png,.mp3",
        categoryID = "A"
    )
    private Document documentLink;


    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public CICDocument(@JsonProperty("documentEmailContent") String documentEmailContent,
                       @JsonProperty("documentLink") Document documentLink) {
        this.documentEmailContent = documentEmailContent;
        this.documentLink = documentLink;


    }
}
