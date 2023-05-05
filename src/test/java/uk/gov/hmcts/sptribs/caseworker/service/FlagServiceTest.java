package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getApplicantFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRepresentativeFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getSubjectFlags;

@ExtendWith(MockitoExtension.class)
class FlagServiceTest {

    @InjectMocks
    private FlagService flagService;

    @Test
    void shouldPopulateFlagDynamicList() {
        CaseData data = caseData();
        data.setApplicantFlags(getApplicantFlags());
        data.setRepresentativeFlags(getRepresentativeFlags());
        data.setSubjectFlags(getSubjectFlags());
        data.setCaseFlags(getCaseFlags());

        //When
        DynamicList result = flagService.populateFlagList(data);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getListItems()).hasSize(4);
    }

}
