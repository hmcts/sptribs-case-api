package uk.gov.hmcts.sptribs.document.bundling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import uk.gov.hmcts.sptribs.document.bundling.model.BundleDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BundleSubFolder2 {

    private String name;
    @JsonUnwrapped(prefix = "documents")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = Collection,
        typeParameterOverride = "BundleDocument")
    private List<ListValue<BundleDocument>> documents = new ArrayList<>();
    private int sortIndex;
}
