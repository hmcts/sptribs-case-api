package uk.gov.hmcts.sptribs.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    List<CaseworkerCICDocument> latestCaseBundleDocuments;
    List<CaseworkerCICDocument> contactPartiesDocuments;
    List<CaseworkerCICDocument> orderAndDecisionDocuments;

 /*   the Latest case bundle
    Documents that have already been sent to the applicant via 'Contact parties'
    All orders and decisions
*/
}
