package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
public class CaseworkerDssUpdateCaseTest {

    @InjectMocks
    private CaseworkerDssUpdateCase caseworkerDssUpdateCase;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AppsConfig.AppsDetails cicAppDetail;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        cicAppDetail = new AppsConfig.AppsDetails();
        cicAppDetail.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        cicAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setDssUpdateEvent("caseworker-update-dss-application");

        cicAppDetail.setEventIds(eventsConfig);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(appsConfig.getApps()).thenReturn(Arrays.asList(cicAppDetail));
        caseworkerDssUpdateCase.setDssUpdateCaseEnabled(true);

        caseworkerDssUpdateCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("caseworker-update-dss-application");
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getDescription)
            .contains("Application DSS Update (cic)");
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getName)
            .contains("DSS Update case (cic)");
    }
}
