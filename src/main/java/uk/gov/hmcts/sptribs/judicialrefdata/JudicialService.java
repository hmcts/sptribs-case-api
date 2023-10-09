package uk.gov.hmcts.sptribs.judicialrefdata;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.Judge;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static java.util.Comparator.comparing;
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

    public DynamicList getAllUsers(CaseData caseData) {
        final var users = getUsers();
        final var judges = populateJudgesList(users);
        caseData.getListing().getSummary().setJudgeList(judges);
        return populateUsersDynamicList(judges);
    }

    private List<UserProfileRefreshResponse> getUsers() {

        try {
            List<UserProfileRefreshResponse> list = judicialClient.getUserProfiles(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                JudicialUsersRequest.builder()
                    .ccdServiceName(SERVICE_NAME)
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

    private List<ListValue<Judge>> populateJudgesList(List<UserProfileRefreshResponse> userProfiles) {
        AtomicInteger listValueIndex = new AtomicInteger(0);
        return userProfiles.stream()
            .map(userProfile -> ListValue.<Judge>builder()
                    .id(String.valueOf(listValueIndex.incrementAndGet()))
                    .value(Judge.builder()
                            .uuid(UUID.randomUUID().toString())
                            .judgeFullName(userProfile.getFullName())
                            .personalCode(userProfile.getPersonalCode())
                            .build()
                    )
                    .build()
            )
            .collect(Collectors.toList());
    }

    private DynamicList populateUsersDynamicList(List<ListValue<Judge>> judges) {
        List<DynamicListElement> usersDynamicList =
            judges.stream()
                .map(ListValue::getValue)
                .sorted(comparing(Judge::getJudgeFullName))
                .map(user -> DynamicListElement.builder().label(user.getJudgeFullName()).code(UUID.fromString(user.getUuid())).build())
                .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(usersDynamicList)
            .build();
    }

    public String populateJudicialId(CaseData caseData) {
        UUID selectedJudgeUuid = caseData.getListing().getSummary().getJudge().getValueCode();
        Optional<String> judgeJudicialId = caseData.getListing().getSummary().getJudgeList().stream()
            .map(ListValue::getValue)
            .filter(j -> selectedJudgeUuid.equals(UUID.fromString(j.getUuid())))
            .findFirst()
            .map(Judge::getPersonalCode);

        return judgeJudicialId.orElse("");
    }
}
