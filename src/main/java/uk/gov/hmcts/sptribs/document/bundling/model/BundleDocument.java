package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class BundleDocument {

    @CCD(
        label = "Document Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String name;

    @CCD(
        label = "Short Description",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String description;

    @CCD(
        label = "Sort Index",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private int sortIndex;

    @CCD(
        label = "Source Document",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document sourceDocument;
}
