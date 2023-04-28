package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getAppellantFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRespondentFlags;

@ExtendWith(MockitoExtension.class)
class FlagServiceTest {

    @InjectMocks
    private FlagService flagService;

    @Test
    void shouldPopulateFlagDynamicList() {

        CicCase cicCase = CicCase.builder()
            .appellantFlags(getAppellantFlags())
            .respondentFlags(getRespondentFlags())
            .caseFlags(getCaseFlags())
            .build();

        //When

        DynamicList result = flagService.populateFlagList(cicCase);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getListItems()).hasSize(3);
    }

}
