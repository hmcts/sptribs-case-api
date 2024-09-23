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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.CloseReason;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingDate;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted.MARKASCOMPLETED;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingFormat.FACE_TO_FACE;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingType.FINAL;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingType.INTERLOCUTORY;
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

    public static CicCase cicCase() {
        return CicCase.builder()
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

    public static CallbackRequest callbackRequest(final CaseData caseData, final CaseData caseDataBefore, final String eventId) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(caseDetailsBefore(caseDataBefore))
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
        listing.setHearingTime("10:00");
        listing.setDate(LocalDate.of(2023, 4, 21));
        listing.setHearingStatus(HearingState.Listed);
        listing.setNumberOfDays(YesOrNo.NO);
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

    public static List<ListValue<DateModel>> getDueDateList() {
        DateModel date = DateModel.builder()
            .dueDate(LocalDate.of(2024, 9, 5))
            .information(HEARING_TIME)
            .orderMarkAsCompleted(Set.of(MARKASCOMPLETED))
            .build();
        List<ListValue<DateModel>> list = new ArrayList<>();
        ListValue<DateModel> listValue1 = ListValue.<DateModel>builder().value(date).id("0").build();
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

    public static List<ListValue<CaseworkerCICDocument>> getDocument() {
        List<ListValue<CaseworkerCICDocument>> listValueList = get2Document();
        ListValue<CaseworkerCICDocument> last = listValueList.get(1);
        listValueList.remove(last);
        return listValueList;
    }

    public static List<ListValue<CaseworkerCICDocument>> get2Document() {
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

    public static List<ListValue<CICDocument>> get2DocumentCiC() {
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

    public static ListValue<CICDocument> getCICDocument(String fileName) {
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentEmailContent("some email content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        return documentListValue;
    }

    public static ListValue<CaseworkerCICDocument> getCaseworkerCICDocument(String fileName) {
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        return documentListValue;
    }

    public static ListValue<CaseworkerCICDocumentUpload> getCaseworkerCICDocumentUpload(String fileName) {
        final CaseworkerCICDocumentUpload document = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        return documentListValue;
    }

    public static List<ListValue<CICDocument>> getCICDocumentList(String fileName) {
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentEmailContent("some email content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        return documentList;
    }

    public static List<ListValue<CICDocument>> getCICDocumentListWithUrl(String fileName, String url) {
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder()
                .filename(fileName)
                .binaryUrl(url)
                .url(url)
                .build())
            .documentEmailContent("some email content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentList(String fileName) {
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);
        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentList(String fileName, DocumentType category) {
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentCategory(category)
            .documentEmailContent("some email content")
            .build();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);
        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentList() {
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder()
                .filename("test.pdf")
                .binaryUrl("http://url/")
                .url("http://url/")
                .build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);
        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocumentUpload>> getCaseworkerCICDocumentUploadList(String fileName) {
        final CaseworkerCICDocumentUpload caseworkerCICDocument = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocumentUpload> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);
        return documentList;
    }

    public static DynamicMultiSelectList getDynamicMultiSelectDocumentListWithXElements(int numberOfDocuments) {
        List<DynamicListElement> elements = new ArrayList<>();
        for (int i = 0; i < numberOfDocuments; i++) {
            {
                final DynamicListElement listItem = DynamicListElement
                    .builder()
                    .label("" + UUID.randomUUID())
                    .code(UUID.randomUUID())
                    .build();
                elements.add(listItem);
            }
        }
        return DynamicMultiSelectList
            .builder()
            .value(elements)
            .listItems(elements)
            .build();
    }

    public static List<ListValue<PanelMember>> getMembers() {
        List<ListValue<PanelMember>> members = new ArrayList<>();
        ListValue<PanelMember> member = new ListValue<>();
        PanelMember panelMember1 = PanelMember.builder()
            .name(getDynamicListMembers())
            .build();
        member.setValue(panelMember1);
        members.add(member);
        return members;
    }

    public static DynamicList getDynamicListMembers() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("Jane Doe")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    public static DssCaseData getDssCaseData() {
        EdgeCaseDocument doc1 = new EdgeCaseDocument();
        doc1.setDocumentLink(
            Document.builder()
                .filename("doc1.pdf")
                .binaryUrl("doc1.pdf/binary")
                .categoryId("test category")
                .build()
        );
        doc1.setComment("this doc is relevant to the case");
        EdgeCaseDocument doc2 = new EdgeCaseDocument();
        doc2.setDocumentLink(
            Document.builder()
                .filename("doc2.pdf")
                .binaryUrl("doc2.pdf/binary")
                .categoryId("test category")
                .build()
        );
        doc2.setComment("this doc is also relevant to the case");
        final List<ListValue<EdgeCaseDocument>> dssCaseDataOtherInfoDocuments = List.of(
            new ListValue<>("1", doc1),
            new ListValue<>("2", doc2)
        );

        return DssCaseData.builder()
            .additionalInformation("some additional info")
            .otherInfoDocuments(dssCaseDataOtherInfoDocuments)
            .build();
    }

    public static List<ListValue<Listing>> getHearingList() {
        final Listing listing1 = Listing.builder()
            .date(LocalDate.of(2024, 8, 14))
            .hearingType(FINAL)
            .hearingTime("10:00")
            .regionList(getMockedRegionData())
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(emptySet())
            .roomAtVenue("G.01")
            .addlInstr("Ground floor")
            .hearingFormat(FACE_TO_FACE)
            .shortNotice(YES)
            .hearingStatus(Listed)
            .build();
        final Listing listing2 = Listing.builder()
            .date(LocalDate.of(2024, 8, 14))
            .hearingType(INTERLOCUTORY)
            .hearingTime("14:00")
            .regionList(getMockedRegionData())
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(emptySet())
            .roomAtVenue("G.01")
            .addlInstr("Ground floor")
            .hearingFormat(FACE_TO_FACE)
            .shortNotice(YES)
            .hearingStatus(Listed)
            .build();

        final List<ListValue<Listing>> hearingList = new ArrayList<>();
        hearingList.add(new ListValue<>("1", listing1));
        hearingList.add(new ListValue<>("2", listing2));

        return hearingList;
    }
}
