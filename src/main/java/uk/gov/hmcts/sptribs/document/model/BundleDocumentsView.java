package uk.gov.hmcts.sptribs.document.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class BundleDocumentsView {
    List<CaseworkerCICDocument> allDocuments;
    List<CaseworkerCICDocument> initialDocuments;
    List<CaseworkerCICDocument> furtherDocuments;
}
