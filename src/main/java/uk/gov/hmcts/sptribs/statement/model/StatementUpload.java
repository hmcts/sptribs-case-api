package uk.gov.hmcts.sptribs.statement.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.DynamicRadioList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class StatementUpload {

    @CCD(
        label = "Statement from",
        typeOverride = DynamicRadioList,
        access = {DefaultAccess.class}
    )
    private DynamicList statementParty;

    @CCD(
        label = "Statement document",
        access = {DefaultAccess.class}
    )
    private Document statementDocument;
}
