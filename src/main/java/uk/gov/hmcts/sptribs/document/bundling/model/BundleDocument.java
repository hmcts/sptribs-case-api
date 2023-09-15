package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BundleDocument {

    @CCD(
        label = "Document Name"
    )
    private String name;
    @CCD(
        label = "Short Description"
    )
    private String description;
    @CCD(
        label = "Sort Index"
    )
    private int sortIndex;
    @CCD(
        label = "Source Document"
    )
    private Document sourceDocument;
}
