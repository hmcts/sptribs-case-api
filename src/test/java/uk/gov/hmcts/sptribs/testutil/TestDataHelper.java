package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.CloseReason;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_2;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

public class TestDataHelper {

    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 4, 28, 1, 0);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private TestDataHelper() {

    }

    public static CaseData caseData() {
        return CaseData.builder()
            .build();
    }

    public static CaseData closedCaseData() {
        return CaseData.builder()
            .caseStatus(State.CaseManagement)
            .build();
    }

    public static CaseData awaitingOutcomeData() {

        CloseCase closeCase = new CloseCase();
        closeCase.setCloseCaseReason(CloseReason.Rejected);
        closeCase.setAdditionalDetail("case rejected");

        return CaseData.builder()
            .caseStatus(State.AwaitingOutcome)
            .closeCase(closeCase)
            .build();
    }

    public static CallbackRequest callbackRequest(CaseData caseData) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        return CallbackRequest
            .builder()
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData,
                                                  final String eventId) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(caseDetailsBefore(caseData))
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData, String eventId, String state) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                caseDetailsBefore(caseData))
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .state(state)
                    .id(TEST_CASE_ID)
                    .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
                    .build()
            )
            .build();
    }

    public static FeignException feignException(int status, String reason) {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        return FeignException.errorStatus(
            "idamRequestFailed",
            Response.builder()
                .request(request)
                .status(status)
                .headers(Collections.emptyMap())
                .reason(reason)
                .build()
        );
    }

    private static CaseDetails caseDetailsBefore(CaseData caseData) {
        return CaseDetails
            .builder()
            .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
            .id(TEST_CASE_ID)
            .caseTypeId(CcdCaseType.CIC.getCaseTypeName())
            .build();
    }

    public static User getUser() {
        UserDetails userDetails = UserDetails
            .builder()
            .forename("testFname")
            .surname("testSname")
            .roles(List.of(""))
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }

    public static User getUserWithHmctsJudiciary() {
        UserDetails userDetails = UserDetails
            .builder()
            .forename("testFname")
            .surname("testSname")
            .roles(List.of("hmcts-judiciary"))
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }

    public static User getUserWithSeniorJudge() {
        UserDetails userDetails = UserDetails
            .builder()
            .forename("testFname")
            .surname("testSname")
            .roles(List.of("caseworker-st_cic-senior-judge"))
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }

    public static Listing getRecordListing() {
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setConferenceCallNumber("");
        listing.setHearingType(HearingType.FINAL);
        listing.setImportantInfoDetails("some details");
        listing.setVideoCallLink("");
        listing.setDate(LocalDate.now());
        listing.setHearingTime("10:00");
        return listing;
    }

    public static HearingSummary getHearingSummary() {
        return HearingSummary.builder()
            .build();
    }

    public static Listing getRecordListingWithOneHearingDate() {
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setConferenceCallNumber("");
        listing.setHearingType(HearingType.FINAL);
        listing.setImportantInfoDetails("some details");
        listing.setVideoCallLink("");
        listing.setAdditionalHearingDate(getAdditionalHearingDatesOneDate());
        return listing;
    }


    public static List<ListValue<HearingDate>> getAdditionalHearingDatesOneDate() {
        HearingDate date1 = HearingDate.builder().hearingVenueDate(HEARING_DATE_1).hearingVenueTime(HEARING_TIME).build();
        List<ListValue<HearingDate>> list = new ArrayList<>();
        ListValue<HearingDate> listValue1 = ListValue.<HearingDate>builder().value(date1).id("0").build();
        list.add(listValue1);
        return list;
    }

    public static List<ListValue<HearingDate>> getAdditionalHearingDates() {
        HearingDate date1 = HearingDate.builder().hearingVenueDate(HEARING_DATE_1).hearingVenueTime(HEARING_TIME).build();
        HearingDate date2 = HearingDate.builder().hearingVenueDate(HEARING_DATE_2).hearingVenueTime(HEARING_TIME).build();
        List<ListValue<HearingDate>> list = new ArrayList<>();
        ListValue<HearingDate> listValue1 = ListValue.<HearingDate>builder().value(date1).id("0").build();
        ListValue<HearingDate> listValue2 = ListValue.<HearingDate>builder().value(date2).id("1").build();
        list.add(listValue1);
        list.add(listValue2);
        return list;
    }

    public static DynamicList getDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("0")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    public static DynamicMultiSelectList getDynamicMultiSelectDocumentListWith6Elements() {
        List<DynamicListElement> elements = new ArrayList<>();
        final DynamicListElement listItem0 = DynamicListElement
            .builder()
            .label("0--0")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem0);
        final DynamicListElement listItem1 = DynamicListElement
            .builder()
            .label("1--1")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem1);
        final DynamicListElement listItem2 = DynamicListElement
            .builder()
            .label("2--2")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem2);
        final DynamicListElement listItem3 = DynamicListElement
            .builder()
            .label("3--3")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem3);
        final DynamicListElement listItem4 = DynamicListElement
            .builder()
            .label("4--4")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem4);
        final DynamicListElement listItem5 = DynamicListElement
            .builder()
            .label("1--5")
            .code(UUID.randomUUID())
            .build();
        elements.add(listItem5);

        return DynamicMultiSelectList
            .builder()
            .value(elements)
            .listItems(elements)
            .build();
    }

    public static DynamicMultiSelectList getDynamicMultiSelectDocumentList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("0--0")
            .code(UUID.randomUUID())
            .build();
        return DynamicMultiSelectList
            .builder()
            .value(List.of(listItem))
            .listItems(List.of(listItem))
            .build();
    }

    public static  List<ListValue<CaseworkerCICDocument>> getDocument() {
        List<ListValue<CaseworkerCICDocument>> listValueList = get2Document();
        ListValue<CaseworkerCICDocument> last = listValueList.get(1);
        listValueList.remove(last);
        return listValueList;
    }

    public static  List<ListValue<CaseworkerCICDocument>> get2Document() {
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CaseworkerCICDocument doc2 = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list2 = new ListValue<>();
        list2.setValue(doc2);
        listValueList.add(list2);
        return listValueList;
    }

    public static  List<ListValue<CICDocument>> get2DocumentCiC() {
        List<ListValue<CICDocument>> listValueList = new ArrayList<>();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
            .build();
        ListValue<CICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CICDocument doc2 = CICDocument.builder()
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CICDocument> list2 = new ListValue<>();
        list2.setValue(doc2);
        listValueList.add(list2);
        return listValueList;
    }

    public static DynamicList getMockedRegionData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("1-region")
            .code(UUID.randomUUID())
            .build();

        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }


    public static DynamicList getMockedHearingVenueData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("courtname-courtAddress")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    public static List<ListValue<CICDocument>> getCICDocumentList() {
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().build())
            .documentEmailContent("some email content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentList() {
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);
        return documentList;
    }

    public static List<ListValue<CICDocument>> getCICDocumentListWithInvalidFileFormat() {
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().filename("file.txt").build())
            .documentEmailContent("some email content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentListWithInvalidFileFormat() {
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("file.txt").build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        return documentList;
    }
}
