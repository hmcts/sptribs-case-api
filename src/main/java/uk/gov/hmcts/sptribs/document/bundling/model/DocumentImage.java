package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.ImageRendering;
import uk.gov.hmcts.sptribs.document.model.ImageRenderingLocation;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentImage {
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String docmosisAssetId;
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ImageRenderingLocation imageRenderingLocation;
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Integer coordinateX;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Integer coordinateY;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ImageRendering imageRendering;
}
