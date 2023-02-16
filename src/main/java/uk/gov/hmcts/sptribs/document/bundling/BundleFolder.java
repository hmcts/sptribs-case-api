package uk.gov.hmcts.sptribs.document.bundling;

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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BundleFolder {

    private String name;
    @JsonUnwrapped(prefix = "documents")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<BundleDocument>> documents = new ArrayList<>();
    @JsonUnwrapped(prefix = "folders")
    @Builder.Default
    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<BundleFolder>> folders = new ArrayList<>();
    private int sortIndex;
}
