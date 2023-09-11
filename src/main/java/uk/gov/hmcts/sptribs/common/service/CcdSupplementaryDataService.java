package uk.gov.hmcts.sptribs.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.config.CaseFlagsConfiguration;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class CcdSupplementaryDataService {

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CaseFlagsConfiguration caseFlagsConfiguration;

    @Autowired
    private HttpServletRequest request;

    public void submitSupplementaryDataToCcd(String caseId) {

        Map<String, Map<String, Map<String, Object>>> supplementaryDataUpdates = new HashMap<>();
        supplementaryDataUpdates.put("supplementary_data_updates",
            singletonMap("$set", singletonMap("HMCTSServiceId",
                caseFlagsConfiguration.getHmctsId())));

        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        coreCaseDataApi.submitSupplementaryData(caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            caseId,
            supplementaryDataUpdates);
    }

    public void submitSupplementaryDataRequestToCcd(String caseId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryDataRequest = new HashMap<>();
        supplementaryDataRequest.put("supplementary_data_request",
            singletonMap("$set", singletonMap("HMCTSServiceId",
                caseFlagsConfiguration.getHmctsId())));

        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        coreCaseDataApi.submitSupplementaryData(caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            caseId,
            supplementaryDataRequest);
    }
}
