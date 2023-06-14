package uk.gov.hmcts.sptribs.judicialrefdata;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class JudicialService {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private JudicialClient judicialClient;

    private static final String SERVICE_NAME = "ST_CIC";

    public DynamicList getAllUsers() {
        final var users = getUsers();
        return populateUsersDynamicList(users);
    }

    private UserProfileRefreshResponse[] getUsers() {

        try {
            List<UserProfileRefreshResponse> list = judicialClient.getUserProfiles(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                JudicialUsersRequest.builder()
                    .ccdServiceName(SERVICE_NAME)
                    .build());
            if (CollectionUtils.isEmpty(list)) {
                return new UserProfileRefreshResponse[0];
            }

            return list.toArray(new UserProfileRefreshResponse[0]);
        } catch (FeignException exception) {
            log.error("Unable to get user profile data from reference data with exception {}",
                exception.getMessage());
        }
        return new UserProfileRefreshResponse[0];

    }


    private DynamicList populateUsersDynamicList(UserProfileRefreshResponse... userProfiles) {
        List<String> usersList = Objects.nonNull(userProfiles)
            ? Arrays.asList(userProfiles).stream().map(v -> v.getFullName()).collect(Collectors.toList())
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
