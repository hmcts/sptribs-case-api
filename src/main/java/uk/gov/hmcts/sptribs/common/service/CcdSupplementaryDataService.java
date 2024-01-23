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

import static java.util.Collections.singletonMap;

@Service
public class CcdSupplementaryDataService {

    private IdamService idamService;

    private AuthTokenGenerator authTokenGenerator;

    private CoreCaseDataApi coreCaseDataApi;

    private CaseFlagsConfiguration caseFlagsConfiguration;

    private AuthorisationService authorisationService;

    private static final String SUPPLEMENTARY_FIELD = "supplementary_data_updates";
    private static final String SERVICE_ID_FIELD = "HMCTSServiceId";
    private static final String SET_OPERATION = "$set";

    @Autowired
    public CcdSupplementaryDataService(IdamService idamService, AuthTokenGenerator authTokenGenerator,
            CoreCaseDataApi coreCaseDataApi, CaseFlagsConfiguration caseFlagsConfiguration,
            AuthorisationService authorisationService) {
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
        this.caseFlagsConfiguration = caseFlagsConfiguration;
        this.authorisationService = authorisationService;
    }

    public void submitSupplementaryDataToCcd(String caseId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryDataUpdates = new HashMap<>();
        supplementaryDataUpdates.put(SUPPLEMENTARY_FIELD,
            singletonMap(SET_OPERATION, singletonMap(SERVICE_ID_FIELD,
                caseFlagsConfiguration.getHmctsId())));

        coreCaseDataApi.submitSupplementaryData(authorisationService.getAuthorisation(),
            authTokenGenerator.generate(),
            caseId,
            supplementaryDataUpdates);
    }

    public void submitSupplementaryDataRequestToCcd(String caseId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryDataRequest = new HashMap<>();
        supplementaryDataRequest.put(SUPPLEMENTARY_FIELD,
            singletonMap(SET_OPERATION, singletonMap(SERVICE_ID_FIELD,
                caseFlagsConfiguration.getHmctsId())));

        final User caseworkerUser = idamService.retrieveSystemUpdateUserDetails();

        coreCaseDataApi.submitSupplementaryData(caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            caseId,
            supplementaryDataRequest);
    }
}
