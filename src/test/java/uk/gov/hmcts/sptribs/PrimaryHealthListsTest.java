package uk.gov.hmcts.sptribs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.sptribs.ciccase.PrimaryHealthLists;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.PrimaryHealthListsData;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createPrimaryHealthListsDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
class PrimaryHealthListsTest {

    @InjectMocks
    private PrimaryHealthLists primaryHealthLists;

    @Mock
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

    @Test
    @ExtendWith(MockitoExtension.class)
    void shouldBuildConfigWithCorrectCcdCaseType() {
        //Given
        final ConfigBuilderImpl<PrimaryHealthListsData, State, UserRole> configBuilder = createPrimaryHealthListsDataConfigBuilder();
        Mockito.when(cfgs.iterator()).thenReturn(Collections.emptyIterator());

        //When
        primaryHealthLists.configure(configBuilder);

        //Then
        assertThat(configBuilder.build().getCaseType()).isEqualTo(CcdCaseType.PHL.name());

    }

}
