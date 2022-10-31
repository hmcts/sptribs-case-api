package uk.gov.hmcts.sptribs.solicitor.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;

@Service
@Slf4j
public class CcdAccessService {

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public boolean isApplicant1(String userToken, Long caseId) {
        log.info("Retrieving roles for user on case {}", caseId);
        User user = idamService.retrieveUser(userToken);
        List<String> userRoles =
            caseAssignmentApi.getUserRoles(
                    userToken,
                    authTokenGenerator.generate(),
                    List.of(String.valueOf(caseId)),
                    List.of(user.getUserDetails().getId())
                )
                .getCaseAssignmentUserRoles()
                .stream()
                .map(CaseAssignmentUserRole::getCaseRole)
                .collect(Collectors.toList());
        return userRoles.contains(CREATOR.getRole());
    }

}
