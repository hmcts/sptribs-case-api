package uk.gov.hmcts.sptribs.caseworker.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.ciccase.model.ExtendedCaseDetails;
import uk.gov.hmcts.sptribs.services.model.AuditEventsResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@FeignClient(
    name = "core-case-data-api",
    primary = false,
    url = "${core_case_data.api.url}"
)
public interface ExtendedCaseDataApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String EXPERIMENTAL = "experimental=true";

    @GetMapping(
        path = "/cases/{cid}",
        headers = EXPERIMENTAL
    )
    ExtendedCaseDetails getExtendedCaseDetails(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("cid") String caseId
    );

    @GetMapping("/cases/{caseId}/events")
    AuditEventsResponse getAuditEvents(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("experimental") boolean experimental,
        @PathVariable("caseId") String caseId
    );
}
