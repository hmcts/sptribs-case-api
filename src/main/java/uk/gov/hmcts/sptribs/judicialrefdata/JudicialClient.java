package uk.gov.hmcts.sptribs.judicialrefdata;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "judicial-client", url = "${judicial.api.baseUrl}")
public interface JudicialClient {

    @PostMapping(value = "/refdata/judicial/users",
               headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    ResponseEntity<UserProfileRefreshResponse[]> getUserProfiles(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestBody JudicialUsersRequest judicialUsersRequest
    );

}
