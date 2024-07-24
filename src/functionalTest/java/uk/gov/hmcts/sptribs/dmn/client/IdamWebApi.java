package uk.gov.hmcts.sptribs.dmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.dmn.config.FeignConfiguration;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.Token;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.UserInfo;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "idam-web-api",
    url = "${idam.api.baseUrl}",
    configuration = FeignConfiguration.class
)
public interface IdamWebApi {
    @GetMapping(
        value = "/o/userinfo",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    UserInfo userInfo(@RequestHeader(AUTHORIZATION) String userToken);

    @PostMapping(
        value = "/o/token",
        produces = APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Token token(@RequestBody Map<String, ?> form);

    @PostMapping(
        value = "/testing-support/accounts",
        consumes = APPLICATION_JSON_VALUE
    )
    void createTestUser(@RequestBody Map<String, ?> form);

    @DeleteMapping(
        value = "/testing-support/accounts/{username}",
        consumes = APPLICATION_JSON_VALUE
    )
    void deleteTestUser(@PathVariable("username") String username);

}
