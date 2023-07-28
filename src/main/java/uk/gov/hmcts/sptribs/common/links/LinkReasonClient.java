package uk.gov.hmcts.sptribs.common.links;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.ccd.sdk.type.LinkReason;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CATEGORY_ID;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "link-reason-client", url = "${refdata_common.api.baseUrl}")
public interface LinkReasonClient {

    @GetMapping(value = "/refdata/commondata/lov/categories",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    List<LinkReason> getLinkReasons(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestHeader(AUTHORIZATION) final String authorisation,
        @RequestParam(CATEGORY_ID) final String categoryId
    );


}
