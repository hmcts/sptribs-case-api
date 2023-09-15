package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bundle {
    @CCD(
        label = "Bundle ID"
    )
    private String id;
    @CCD(
        label = "Config used for bundle"
    )
    private String title;
    @CCD(
        label = "Description",
        typeOverride = TextArea
    )
    private String description;
    @CCD(
        label = "Stitched Document"
    )
    private Document stitchedDocument;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Bundled Documents",
        typeOverride = Collection,
        typeParameterOverride = "BundleDocument")
    private List<ListValue<BundleDocument>> documents;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Bundle Folders",
        typeOverride = Collection,
        typeParameterOverride = "BundleFolder")
    private List<ListValue<BundleFolder>> folders;

    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Pagination Style",
        typeOverride = FixedList,
        typeParameterOverride = "BundlePaginationStyle"
    )
    private BundlePaginationStyle paginationStyle = BundlePaginationStyle.off;

    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "PAge Number Formet",
        typeOverride = FixedList,
        typeParameterOverride = "PageNumberFormat"
    )
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    @CCD(
        label = "Error from stitching service"
    )
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;
    @CCD(
        label = "Stitch status"
    )
    private String stitchStatus;
    @CCD(
        label = "Is this the bundle you want to amend?"
    )
    private YesOrNo eligibleForStitching;
    @CCD(
        label = "Is this the bundle you want to clone?"
    )
    private YesOrNo eligibleForCloning;
    @CCD(
        label = "Should this bundle have coversheets separating each document?"
    )
    private YesOrNo hasCoversheets;
    @CCD(
        label = "Should this bundle have a title page with a table of contents?"
    )
    private YesOrNo hasTableOfContents;
    @CCD(
        label = "Should this bundle’s folders have a coversheet?"
    )
    private YesOrNo hasFolderCoversheets;
    @CCD(
        label = "Should this bundle be notified by email?"
    )
    private YesOrNo enableEmailNotification;
    @CCD(
        label = "Name of the PDF file"
    )
    private String fileName;
    @CCD(
        label = "Identifier of the PDF file"
    )
    private String fileNameIdentifier;
    @CCD(
        label = "Cover page template"
    )
    private String coverpageTemplate;
}
