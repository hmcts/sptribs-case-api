package uk.gov.hmcts.sptribs.citizen.event;

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
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CicUpdateCaseEventTest {
    @InjectMocks
    private CicUpdateCaseEvent cicUpdateCaseEvent;

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

        AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent("citizen-cic-update-dss-application");

        cicAppDetail.setEventIds(eventsConfig);

    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();


        when(appsConfig.getApps()).thenReturn(Arrays.asList(cicAppDetail));

        cicUpdateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("citizen-cic-update-dss-application");
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getDescription)
            .contains("Application update (cic)");
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getName)
            .contains("Update case (cic)");
    }

    @Test
    void shouldUpdateCaseDetails() {
        //Given
        EdgeCaseDocument dssDoc = new EdgeCaseDocument();
        dssDoc.setDocumentLink(Document.builder().build());
        ListValue<EdgeCaseDocument> listValue = new ListValue<>();
        listValue.setValue(dssDoc);
        DssCaseData dssCaseData = DssCaseData.builder()
            .caseTypeOfApplication(CASE_DATA_CIC_ID)
            .otherInfoDocuments(List.of(listValue))
            .supportingDocuments(List.of(listValue))
            .tribunalFormDocuments(List.of(listValue))
            .subjectFullName(TEST_FIRST_NAME)
            .representation(YesOrNo.YES)
            .representationQualified(YesOrNo.YES)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();

        CicCase cicCase = CicCase.builder().build();
        CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDssCaseData(dssCaseData);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            cicUpdateCaseEvent.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1).isNotNull();
        assertThat(response1.getData().getDssCaseData().getOtherInfoDocuments()).isEmpty();
        assertThat(response1.getData().getCicCase().getApplicantDocumentsUploaded()).hasSize(3);
        assertThat(response1.getData().getCicCase().getRepresentativeContactDetailsPreference()).isEqualTo(ContactPreferenceType.EMAIL);
        assertThat(response1.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue().getDocumentCategory())
            .isIn(DocumentType.DSS_OTHER, DocumentType.DSS_SUPPORTING, DocumentType.DSS_TRIBUNAL_FORM);
    }
}
