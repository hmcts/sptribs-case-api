package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        label = "File"
    )
    private Document documentLink;


    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public CICDocument(@JsonProperty("documentEmailContent") String documentEmailContent,
                       @JsonProperty("documentLink") Document documentLink) {
        this.documentEmailContent = documentEmailContent;
        this.documentLink = documentLink;
    }

    @JsonIgnore
    public boolean isDocumentValid() {
        String regex = ".pdf,.tif,.tiff,.jpg,.jpeg,.png,.mp3,.mp4";
        String fileName = this.documentLink.getFilename();
        String fileExtension = StringUtils.substringAfter(fileName, ".");

        return regex.contains(fileExtension);
    }
}
