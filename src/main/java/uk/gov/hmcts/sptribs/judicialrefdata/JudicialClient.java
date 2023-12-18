package uk.gov.hmcts.sptribs.judicialrefdata;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;
import uk.gov.hmcts.sptribs.services.cdam.FeignSupportConfig;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.ACCEPT;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "judicial-client", url = "${judicial.api.baseUrl}", configuration = FeignSupportConfig.class)
public interface JudicialClient {

    // TODO: Once v2 enabled in all envs:
    //  - Delete getUserProfilesV2
    //  - Add Accept request header to getUserProfiles
    @PostMapping(value = "/refdata/judicial/users",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    List<UserProfileRefreshResponse> getUserProfiles(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestBody JudicialUsersRequest judicialUsersRequest
    );

    @PostMapping(value = "/refdata/judicial/users",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    List<UserProfileRefreshResponse> getUserProfilesV2(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestHeader(ACCEPT) final String accept,
        @RequestBody JudicialUsersRequest judicialUsersRequest
    );
}
