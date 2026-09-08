package uk.gov.hmcts.sptribs.document.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SelectedCaseDocuments {
    List<Long> documentEntityIds;
    List<CaseworkerCICDocument> documents;
}
