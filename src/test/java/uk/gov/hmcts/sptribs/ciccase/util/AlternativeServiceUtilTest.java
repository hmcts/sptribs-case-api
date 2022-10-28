package uk.gov.hmcts.sptribs.ciccase.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.AlternativeService;
import uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.sptribs.ciccase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;


class AlternativeServiceUtilTest {

    @Test
    public void shouldAddNewServiceApplicationToCollectionAndSetApplicationToNull() {
        //Given
        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);

        //When
        caseData.archiveAlternativeServiceApplicationOnCompletion();

        //Then
        assertThat(caseData.getAlternativeServiceOutcomes()).isNotNull();
        assertThat(caseData.getAlternativeServiceOutcomes().size()).isEqualTo(1);
        ListValue<AlternativeServiceOutcome> alternativeServiceOutcome = caseData.getAlternativeServiceOutcomes().get(0);
        assert (alternativeServiceOutcome.getValue().getAlternativeServiceType().equals(DEEMED));
        assertThat(alternativeServiceOutcome.getId()).isNotNull();
        assertThat(alternativeServiceOutcome.getValue().getReceivedServiceAddedDate()).isNotNull();
        assertThat(caseData.getAlternativeService()).isNull();
    }

    @Test
    public void shouldAddSecondServiceApplicationToCollectionIfOneExists() {
        //Given
        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);
        caseData.archiveAlternativeServiceApplicationOnCompletion();

        caseData.setAlternativeService(new AlternativeService());
        caseData.getAlternativeService().setAlternativeServiceType(DISPENSED);
        caseData.archiveAlternativeServiceApplicationOnCompletion();

        caseData.setAlternativeService(new AlternativeService());
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);
        caseData.getAlternativeService().getBailiff().setSuccessfulServedByBailiff(YesOrNo.YES);

        //When
        caseData.archiveAlternativeServiceApplicationOnCompletion();

        //Then
        assertThat(caseData.getAlternativeServiceOutcomes().size()).isEqualTo(3);
        assertThat(caseData.getAlternativeServiceOutcomes().get(0).getValue().getAlternativeServiceType()).isEqualTo(BAILIFF);
        assertThat(caseData.getAlternativeServiceOutcomes().get(0).getValue().getSuccessfulServedByBailiff())
            .isEqualTo(YesOrNo.YES);
        assertThat(caseData.getAlternativeServiceOutcomes().get(1).getValue().getAlternativeServiceType()).isEqualTo(DISPENSED);
        assertThat(caseData.getAlternativeServiceOutcomes().get(2).getValue().getAlternativeServiceType()).isEqualTo(DEEMED);
        assertThat(caseData.getAlternativeService()).isNull();
    }

    @Test
    public void shouldNotAddToServiceApplicationCollectionIfServiceApplicationIsNull() {
        //Given
        final CaseData caseData = caseData();
        caseData.setAlternativeService(null);
        //When
        caseData.archiveAlternativeServiceApplicationOnCompletion();
        //Then
        assertThat(caseData.getAlternativeServiceOutcomes()).isNull();
    }

    @Test
    public void assertIsApplicationGrantedYes() {
        final CaseData caseData = caseData();
        //When
        caseData.getAlternativeService().setServiceApplicationGranted(YesOrNo.YES);
        //Then
        assertThat(caseData.getAlternativeService().isApplicationGranted()).isTrue();
    }

    @Test
    public void assertIsApplicationGrantedNo() {
        final CaseData caseData = caseData();
        //When
        caseData.getAlternativeService().setServiceApplicationGranted(YesOrNo.NO);
        //Then
        assertThat(caseData.getAlternativeService().isApplicationGranted()).isFalse();
    }
}
