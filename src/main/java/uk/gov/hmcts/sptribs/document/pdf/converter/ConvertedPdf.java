package uk.gov.hmcts.sptribs.document.pdf.converter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConvertedPdf {
    private String fileName;
    private byte[] fileContent;
    private CaseworkerCICDocument originalDocument;
}
