package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.*;
import uk.gov.hmcts.ccd.sdk.api.*;
import uk.gov.hmcts.sptribs.DAO.*;
import uk.gov.hmcts.sptribs.document.model.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementRecord {

    @CCD(label = "Party")
    private PartyType partyType;

    @CCD(label = "Statement document")
    private CaseworkerCICDocument statementDocument;
}
