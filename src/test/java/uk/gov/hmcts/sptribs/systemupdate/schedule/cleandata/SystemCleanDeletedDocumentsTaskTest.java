package uk.gov.hmcts.sptribs.systemupdate.schedule.cleandata;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.repository.CaseEventRepository;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

@ExtendWith(MockitoExtension.class)
class SystemCleanDeletedDocumentsTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseEventRepository caseEventRepository;
    @InjectMocks
    private SystemCleanDeletedDocumentsTask systemCleanDeletedDocumentsTask;

}
