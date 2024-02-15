package uk.gov.hmcts.sptribs.judicialrefdata;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.ACCEPT_VALUE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;

@Service
@Slf4j
public class JudicialService {

    private final AuthTokenGenerator authTokenGenerator;

    private final JudicialClient judicialClient;

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public JudicialService(AuthTokenGenerator authTokenGenerator,
            JudicialClient judicialClient, HttpServletRequest httpServletRequest) {
        this.authTokenGenerator = authTokenGenerator;
        this.judicialClient = judicialClient;
        this.httpServletRequest = httpServletRequest;
    }

    public DynamicList getAllUsers(CaseData caseData) {
        final List<UserProfileRefreshResponse> users = getUsers();
        final List<ListValue<Judge>> judges = populateJudgesList(users);
        caseData.getListing().getSummary().setJudgeList(judges);
        return populateUsersDynamicList(judges);
    }

    public String populateJudicialId(CaseData caseData) {
        if (isNull(caseData.getListing().getSummary().getJudge())) {
            return EMPTY_PLACEHOLDER;
        }

        final UUID selectedJudgeUuid = caseData.getListing().getSummary().getJudge().getValueCode();
        final Optional<String> judgeJudicialId = caseData.getListing().getSummary().getJudgeList().stream()
            .map(ListValue::getValue)
            .filter(j -> UUID.fromString(j.getUuid()).equals(selectedJudgeUuid))
            .findFirst()
            .map(Judge::getPersonalCode);

        return judgeJudicialId.orElse(EMPTY_PLACEHOLDER);
    }

    private List<UserProfileRefreshResponse> getUsers() {
        try {
            List<UserProfileRefreshResponse> list =
                judicialClient.getUserProfiles(
                    authTokenGenerator.generate(),
                    httpServletRequest.getHeader(AUTHORIZATION),
                    ACCEPT_VALUE,
                    JudicialUsersRequest.builder()
                        .ccdServiceName(ST_CIC_JURISDICTION)
                        .build()
                );
            if (isEmpty(list)) {
                return new ArrayList<>();
            }
            return list;
        } catch (FeignException exception) {
            log.error(
                "Unable to get user profile data from reference data with exception {}",
                exception.getMessage()
            );
        }
        return new ArrayList<>();
    }

    private List<ListValue<Judge>> populateJudgesList(List<UserProfileRefreshResponse> userProfiles) {
        final AtomicInteger listValueIndex = new AtomicInteger(0);
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
        final List<DynamicListElement> usersDynamicList =
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
}
