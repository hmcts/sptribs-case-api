package uk.gov.hmcts.sptribs.controllers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private CicaCaseResponse cicaCaseResponse;
    private DocumentResponse documentResponse;
}
