package uk.gov.hmcts.sptribs.idam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Getter
@AllArgsConstructor
public class CICUser {
    private String authToken;
    private UserInfo userInfo;
}

