package uk.gov.hmcts.sptribs.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.sptribs.DAO.PartyType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementDTO {
    private Long caseReferenceNumber;
    private PartyType partyType;
    private String documentUrl;
    private String documentFilename;
    private String documentBinaryUrl;
}
