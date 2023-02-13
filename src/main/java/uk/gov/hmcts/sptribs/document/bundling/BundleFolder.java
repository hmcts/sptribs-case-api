package uk.gov.hmcts.sptribs.document.bundling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleFolder {

    private String name;
    private List<CicValue<BundleDocument>> documents = new ArrayList<>();
    private List<CicValue<BundleFolder>> folders = new ArrayList<>();
    private int sortIndex;
}
