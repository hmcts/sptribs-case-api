package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.ASYNC_STITCH_COMPLETE;

@ExtendWith(MockitoExtension.class)
class CaseworkerBundleStitchCompleteTest {

    private static final Long CASE_ID = 12345L;
    private static final Instant instant = Instant.now();
    private static final ZoneId zoneId = ZoneId.systemDefault();

    @Mock
    private DocumentsService documentsService;

    @InjectMocks
    private CaseworkerBundleStitchComplete caseworkerBundleStitchComplete;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerBundleStitchComplete.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(ASYNC_STITCH_COMPLETE);
    }


    @Test
    void shouldSuccessfullySaveBundleToDocumentService() throws Exception {

        //given
        List<ListValue<Bundle>> bundles = new ArrayList<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(CASE_ID);

        Document document = Document.builder()
            .filename("bundle.pdf")
            .binaryUrl("http://url/binary")
            .url("http://url/")
            .build();

        Bundle bundle = createBundle(document, 0L);

        ListValue<Bundle> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(bundle);

        bundles.add(listValue);

        CaseData caseData = new CaseData();
        caseData.setCaseBundles(bundles);
        details.setData(caseData);

        //when
        final SubmittedCallbackResponse stayedResponse = caseworkerBundleStitchComplete.submitted(details, details);

        //then
        assertThat(stayedResponse.getConfirmationHeader()).contains("# documents added successfully");

        verify(documentsService).buildAndSaveNewDocumentEntity(document, CASE_ID, false, true);

    }

    @Test
    void shouldSuccessfullySaveLatestBundleToDocumentService() throws Exception {

        //given
        List<ListValue<Bundle>> bundles = new ArrayList<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(CASE_ID);

        Document document1 = Document.builder()
            .filename("bundle.pdf")
            .binaryUrl("http://url/binary")
            .url("http://url/")
            .build();

        Document document2 = Document.builder()
            .filename("bundle.pdf")
            .binaryUrl("http://url2/binary")
            .url("http://url/")
            .build();

        Bundle bundle1 = createBundle(document1, 0L);
        Bundle bundle2 = createBundle(document2, 1L);

        ListValue<Bundle> listValue1 = new ListValue<>();
        listValue1.setId("1");
        listValue1.setValue(bundle1);

        ListValue<Bundle> listValue2 = new ListValue<>();
        listValue2.setId("2");
        listValue2.setValue(bundle2);

        bundles.add(listValue1);
        bundles.add(listValue2);

        sortBundlesByTime(bundles);

        CaseData caseData = new CaseData();
        caseData.setCaseBundles(bundles);
        details.setData(caseData);

        //when
        final SubmittedCallbackResponse stayedResponse = caseworkerBundleStitchComplete.submitted(details, details);

        //then
        assertThat(stayedResponse.getConfirmationHeader()).contains("# documents added successfully");

        verify(documentsService).buildAndSaveNewDocumentEntity(document2, CASE_ID, false, true);

    }

    @Test
    void shouldReturnErrorResponseWhenDocumentServiceThrowsException() throws Exception {

        // given
        List<ListValue<Bundle>> bundles = new ArrayList<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(CASE_ID);

        Document document = Document.builder()
            .filename("bundle.pdf")
            .binaryUrl("http://url/binary")
            .url("http://url/")
            .build();

        Bundle bundle = createBundle(document, 0L);

        ListValue<Bundle> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(bundle);

        bundles.add(listValue);

        CaseData caseData = new CaseData();
        caseData.setCaseBundles(bundles);
        details.setData(caseData);

        doThrow(new RuntimeException("DB failure"))
            .when(documentsService)
            .buildAndSaveNewDocumentEntity(document, CASE_ID, false, true);

        // when
        SubmittedCallbackResponse response =
            caseworkerBundleStitchComplete.submitted(details, details);

        // then
        assertThat(response.getConfirmationHeader())
            .contains("# Error saving latest case bundle to document entity");

        verify(documentsService)
            .buildAndSaveNewDocumentEntity(document, CASE_ID, false, true);
    }

    @Test
    void shouldNotCallDocumentServiceWhenStitchedDocumentIsNull() {

        // given
        List<ListValue<Bundle>> bundles = new ArrayList<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(CASE_ID);

        Bundle bundle = createBundle(null, 0L);

        ListValue<Bundle> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(bundle);

        bundles.add(listValue);

        CaseData caseData = new CaseData();
        caseData.setCaseBundles(bundles);
        details.setData(caseData);


        //when
        SubmittedCallbackResponse response =
            caseworkerBundleStitchComplete.submitted(details, details);

        //then
        verifyNoInteractions(documentsService);

        assertThat(response.getConfirmationHeader())
            .contains("# No stitched bundle document found");
    }

    private static Bundle createBundle(Document stitchedDocument, long minutes) {
        return Bundle.builder()
            .id("1")
            .dateAndTime(LocalDateTime.now(Clock.fixed(
                instant,
                zoneId)).plusMinutes(minutes))
            .title("")
            .description("")
            .stitchedDocument(stitchedDocument)
            .build();
    }

    private void sortBundlesByTime(List<ListValue<Bundle>> caseBundles) {

        caseBundles.sort(
            Comparator.comparing(
                bundle -> bundle.getValue().getDateAndTime(),
                Comparator.nullsLast(Comparator.reverseOrder())
            )
        );

    }

}
