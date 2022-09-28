package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.BulkScanEnvelope;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;


import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkScanMetaInfo {
    @CCD(
        label = "Transformation and OCR warnings",
        typeOverride = Collection,
        typeParameterOverride = "TextArea",
        access = {CaseworkerAccess.class}
    )
    @Builder.Default
    private List<ListValue<String>> warnings = new ArrayList<>();

    private YesOrNo evidenceHandled;
}
