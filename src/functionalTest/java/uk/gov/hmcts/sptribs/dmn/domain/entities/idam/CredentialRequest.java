package uk.gov.hmcts.sptribs.dmn.domain.entities.idam;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialRequest {
    private String credentialsKey;
    private boolean granularPermission;
}
