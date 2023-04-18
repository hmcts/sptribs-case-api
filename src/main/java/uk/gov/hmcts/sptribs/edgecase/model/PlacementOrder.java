package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.edgecase.model.access.DefaultAccess;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@Builder
public class PlacementOrder {
    @CCD(label = "Placement Order id")
    private String placementOrderId;

    @CCD(label = "Placement Order type")
    private String placementOrderType;

    @CCD(label = "Placement Order Number")
    private String placementOrderNumber;

    @CCD(label = "Placement Order Court")
    private String placementOrderCourt;

    @CCD(
        label = "Placement Order Date",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate placementOrderDate;
}
