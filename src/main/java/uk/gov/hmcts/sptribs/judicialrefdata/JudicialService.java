package uk.gov.hmcts.sptribs.judicialrefdata;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.ACCEPT_VALUE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;

@Service
@Slf4j
public class JudicialService {

    private HttpServletRequest httpServletRequest;

    private AuthTokenGenerator authTokenGenerator;
    
    private JudicialClient judicialClient;
    
    @Value("${toggle.enable_jrd_api_v2}")
    private boolean enableJrdApiV2;
    
    @Autowired
    public JudicialService(HttpServletRequest httpServletRequest, AuthTokenGenerator authTokenGenerator,
            JudicialClient judicialClient) {
        this.httpServletRequest = httpServletRequest;
        this.authTokenGenerator = authTokenGenerator;
        this.judicialClient = judicialClient;
    }

    public DynamicList getAllUsers() {

        final var users = getUsers();
        return populateUsersDynamicList(users);
    }


    private List<UserProfileRefreshResponse> getUsers() {
        try {
            List<UserProfileRefreshResponse> list =
                enableJrdApiV2
                    ? judicialClient.getUserProfilesV2(
                        authTokenGenerator.generate(),
                        httpServletRequest.getHeader(AUTHORIZATION),
                        ACCEPT_VALUE,
                        JudicialUsersRequest.builder()
                            .ccdServiceName(ST_CIC_JURISDICTION)
                            .build())
                    : judicialClient.getUserProfiles(
                        authTokenGenerator.generate(),
                        httpServletRequest.getHeader(AUTHORIZATION),
                        JudicialUsersRequest.builder()
                            .ccdServiceName(ST_CIC_JURISDICTION)
                            .build());
            if (CollectionUtils.isEmpty(list)) {
                return new ArrayList<>();
            }
            return list;
        } catch (FeignException exception) {
            log.error("Unable to get user profile data from reference data with exception {}",
                exception.getMessage());
        }
        return new ArrayList<>();
    }

    private DynamicList populateUsersDynamicList(List<UserProfileRefreshResponse> judges) {
        List<String> usersList = Objects.nonNull(judges)
            ? judges.stream().map(UserProfileRefreshResponse::getFullName).collect(Collectors.toList())
            : new ArrayList<>();

        List<DynamicListElement> usersDynamicList = usersList
            .stream()
            .sorted()
            .map(user -> DynamicListElement.builder().label(user).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(usersDynamicList)
            .build();
    }
}
