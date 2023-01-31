package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CicBundleDocumentDTO {

    private String name;
    private String description;
    private int sortIndex;
    private CICDocument sourceDocument;
}
