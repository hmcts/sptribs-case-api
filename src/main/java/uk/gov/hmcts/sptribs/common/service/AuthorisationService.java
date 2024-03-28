package uk.gov.hmcts.sptribs.common.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Service
@Slf4j
public class AuthorisationService {

    private IdamService idamService;

    private HttpServletRequest httpServletRequest;


    @Autowired
    public AuthorisationService(IdamService idamService, HttpServletRequest httpServletRequest) {
        this.idamService = idamService;
        this.httpServletRequest = httpServletRequest;
    }

    public String getAuthorisation() {
        final User user = idamService.retrieveUser(httpServletRequest.getHeader(AUTHORIZATION));
        return user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();
    }
}
