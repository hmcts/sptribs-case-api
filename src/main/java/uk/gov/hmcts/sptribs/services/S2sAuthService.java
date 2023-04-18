package uk.gov.hmcts.sptribs.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

@Service
public class S2sAuthService {

    @Autowired
    private AuthTokenValidator authTokenValidator;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void tokenValidator(String token) {
        authTokenValidator.validate(token);
    }

    public String serviceAuthTokenGenerator() {
        return authTokenGenerator.generate();
    }
}
