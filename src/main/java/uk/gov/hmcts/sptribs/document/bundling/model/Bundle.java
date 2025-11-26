package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bundle {
    @CCD(
        label = "Bundle ID",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String id;

    @CCD(
        label = "Date and time",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime dateAndTime = LocalDateTime.now();

    @CCD(
        label = "Config used for bundle",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String title;

    @CCD(
        label = "Description",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String description;

    @CCD(
        label = "Stitched Document",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
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
        label = "Page Number Format",
        typeOverride = FixedList,
        typeParameterOverride = "PageNumberFormat"
    )
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;

    @CCD(
        label = "Error from stitching service",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String stitchingFailureMessage;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;


    @CCD(
        label = "Stitch status",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String stitchStatus;

    @CCD(
        label = "Is this the bundle you want to amend?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo eligibleForStitching;

    @CCD(
        label = "Is this the bundle you want to clone?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo eligibleForCloning;

    @CCD(
        label = "Should this bundle have coversheets separating each document?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo hasCoversheets;

    @CCD(
        label = "Does this bundle have a table of contents?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo hasTableOfContents;

    @CCD(
        label = "Should this bundle have coversheets separating each folder?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo hasFolderCoversheets;

    @CCD(
        label = "Should this bundle be notified by email?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo enableEmailNotification;

    @CCD(
        label = "Name of the PDF file",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fileName;

    @CCD(
        label = "Identifier of the PDF file",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fileNameIdentifier;

    @CCD(
        label = "Cover page template",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String coverpageTemplate;
}
