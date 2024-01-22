package uk.gov.hmcts.sptribs.caseworker.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.ExtendedCaseDetails;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class ExtendedCaseDataService {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Autowired
    private ExtendedCaseDataApi caseDataApi;

    public Map<String, Object> getDataClassification(String caseId) {
        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        ExtendedCaseDetails caseDetails = caseDataApi.getExtendedCaseDetails(caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(), caseId);
        return caseDetails.getDataClassification();
    }
}
