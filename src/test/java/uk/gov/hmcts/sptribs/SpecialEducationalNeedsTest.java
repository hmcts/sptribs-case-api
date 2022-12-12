package uk.gov.hmcts.sptribs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.sptribs.ciccase.SpecialEducationalNeeds;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.SpecialEducationalNeedsData;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createSpecialEducationalNeedsDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
class SpecialEducationalNeedsTest {

    @InjectMocks
    private SpecialEducationalNeeds specialEducationalNeeds;

    @Mock
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

    @Test
    @ExtendWith(MockitoExtension.class)
    void shouldBuildConfigWithCorrectCcdCaseType() {
        //Given
        final ConfigBuilderImpl<SpecialEducationalNeedsData, State, UserRole> configBuilder =
            createSpecialEducationalNeedsDataConfigBuilder();
        Mockito.when(cfgs.iterator()).thenReturn(Collections.emptyIterator());

        //When
        specialEducationalNeeds.configure(configBuilder);

        //Then
        assertThat(configBuilder.build().getCaseType()).isEqualTo(CcdCaseType.SEN.getCaseName());

    }

}
