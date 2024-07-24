package uk.gov.hmcts.sptribs.dmn.service;

import io.restassured.http.Headers;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.CredentialRequest;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.UserInfo;

import java.io.IOException;

public interface AuthorizationHeaders {
    Headers getWaSystemUserAuthorization();

    Headers getWaUserAuthorization(CredentialRequest request) throws IOException;

    UserInfo getUserInfo(String userToken);

    void cleanupTestUsers();
}
