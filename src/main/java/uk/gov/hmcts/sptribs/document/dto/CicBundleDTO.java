package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;

import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.Size;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CicBundleDTO {

    private String id;
    private String title;
    @Size(max = 255, message = CommonConstants.BUNDLE_DESCRIPTION_FIELD_LENGTH_ERROR_MSG)
    private String description;
    private CICDocument stitchedDocument;
    private List<CicValue<CicBundleDocumentDTO>> documents = new LinkedList<>();
    private List<CicValue<CicBundleFolderDTO>> folders = new LinkedList<>();
    private CicBundlePaginationStyle paginationStyle = CicBundlePaginationStyle.off;
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;
}
