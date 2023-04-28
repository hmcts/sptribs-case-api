package uk.gov.hmcts.sptribs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentInfo {
    private String url;
    private String fileName;
    private String documentId;
    private String binaryUrl;
}
