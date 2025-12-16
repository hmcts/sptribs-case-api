package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAndSuperUserAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class DraftOrderContentCIC {

    @CCD(
        label = "Template",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "OrderTemplate"
    )
    private OrderTemplate orderTemplate;

    @CCD(
        label = "Main content",
        typeOverride = TextArea,
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String mainContent;

    @CCD(
        label = "Order signature",
        access = {CaseworkerAndSuperUserAccess.class}
    )
    private String orderSignature;
}
