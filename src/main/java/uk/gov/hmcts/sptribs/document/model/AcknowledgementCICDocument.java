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

import java.util.Arrays;

import static java.util.Locale.ROOT;
import static org.apache.commons.lang.StringUtils.substringAfterLast;

@Data
@NoArgsConstructor
@Builder
public class AcknowledgementCICDocument {

    @CCD(
        label = "File",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document documentLink;

    @JsonCreator
    public AcknowledgementCICDocument(@JsonProperty("documentLink") Document documentLink) {
        this.documentLink = documentLink;
    }

    @JsonIgnore
    public boolean isDocumentValid() {
        return isDocumentValid("pdf,csv,txt,rtf,xlsx,docx,doc,xls,mp3,m4a,mp4,jpg,jpeg,bmp,tif,tiff,png");
    }

    public boolean isDocumentValid(String validExtensions) {
        String fileName = this.documentLink.getFilename();
        String fileExtension = substringAfterLast(fileName, ".");
        return fileExtension != null && Arrays.asList(validExtensions.split(",")).contains(fileExtension.toLowerCase(ROOT));
    }
}
