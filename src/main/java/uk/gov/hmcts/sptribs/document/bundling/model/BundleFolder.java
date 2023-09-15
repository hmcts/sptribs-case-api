package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BundleFolder {
    @CCD(
        label = "Folder Name"
    )
    private String name;
    @CCD(
        label = "Folder Documents"
    )
    private List<ListValue<BundleDocument>> documents;
    @CCD(
        label = "Sub Folders"
    )
    private List<ListValue<BundleSubFolder>> folders;
    @CCD(
        label = "Sort Index"
    )
    private int sortIndex;
}
