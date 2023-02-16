package uk.gov.hmcts.sptribs.document.bundling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.Size;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bundle {

    private String id;
    private String title;
    @Size(max = 255, message = CommonConstants.BUNDLE_DESCRIPTION_FIELD_LENGTH_ERROR_MSG)
    private String description;
    private CICDocument stitchedDocument;
    @JsonUnwrapped(prefix = "documents")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<BundleDocument>> documents = new LinkedList<>();
    @JsonUnwrapped(prefix = "folders")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<BundleFolder>> folders = new LinkedList<>();
    @Builder.Default
    private BundlePaginationStyle paginationStyle = BundlePaginationStyle.off;
    @Builder.Default
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;
}
