package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CicBundleFolderDTO {

    private String name;
    private List<CicValue<CicBundleDocumentDTO>> documents = new ArrayList<>();
    private List<CicValue<CicBundleFolderDTO>> folders = new ArrayList<>();
    private int sortIndex;
}
