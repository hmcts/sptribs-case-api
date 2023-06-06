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

    private String id;
    private String title;
    @CCD(
        label = "Description",
        typeOverride = TextArea
    )
    private String description;
    private Document stitchedDocument;
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = Collection,
        typeParameterOverride = "BundleDocument")
    private List<ListValue<BundleDocument>> documents;
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = Collection,
        typeParameterOverride = "BundleFolder")
    private List<ListValue<BundleFolder>> folders;
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList
    )
    private BundlePaginationStyle paginationStyle = BundlePaginationStyle.off;
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList
    )
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;

    private String stitchStatus;
    private YesOrNo eligibleForStitching;
    private YesOrNo eligibleForCloning;
    private YesOrNo hasCoversheets;
    private YesOrNo hasTableOfContents;
    private YesOrNo hasFolderCoversheets;
    private YesOrNo enableEmailNotification;
    private String fileName;
    private String fileNameIdentifier;
    private String coverpageTemplate;
}
