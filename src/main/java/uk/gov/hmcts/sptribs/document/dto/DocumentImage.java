package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.sptribs.document.model.ImageRendering;
import uk.gov.hmcts.sptribs.document.model.ImageRenderingLocation;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentImage {
    private String docmosisAssetId;
    private ImageRenderingLocation imageRenderingLocation;
    private Integer coordinateX;
    private Integer coordinateY;
    private ImageRendering imageRendering;
}
