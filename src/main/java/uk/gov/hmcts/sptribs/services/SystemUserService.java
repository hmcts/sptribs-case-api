package uk.gov.hmcts.sptribs.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final IdamClient idamClient;

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }
}
