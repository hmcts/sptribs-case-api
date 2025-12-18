package uk.gov.hmcts.sptribs.caseworker.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesAllowedFileTypes;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.getDecisionDocs;
import static uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil.getFinalDecisionDocs;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentManagementUtil.buildListValues;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.OrderDocumentListUtil.getOrderDocuments;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CLOSE_CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOC_MGMT_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.HEARING_SUMMARY_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.REINSTATE_TYPE;

public final class DocumentListUtil {

    private static final String DOCUMENT_BINARY_PATH = "documents/%s/binary";

    private DocumentListUtil() {

    }

    private static List<CaseworkerCICDocument> prepareList(CaseData data) {
        List<CaseworkerCICDocument> docList = new ArrayList<>(getDocumentManagementDocs(data));
        prepareOtherCaseDocuments(data, docList);
        return docList;
    }

    private static List<CaseworkerCICDocument> prepareListExcludingInitialCicaUpload(CaseData data) {
        List<CaseworkerCICDocument> docList =  data.getFurtherUploadedDocuments() != null ? data.getFurtherUploadedDocuments().stream()
            .map(ListValue::getValue)
            .collect(Collectors.toList()) : new ArrayList<>();
        prepareOtherCaseDocuments(data, docList);
        return docList;
    }

    private static void prepareOtherCaseDocuments(CaseData data, List<CaseworkerCICDocument> docList) {
        docList.addAll(getOrderDocuments(data.getCicCase()));
        docList.addAll(getCaseDocs(data.getCicCase()));
        docList.addAll(getReinstateDocuments(data.getCicCase()));
        docList.addAll(getDecisionDocs(data));
        docList.addAll(getFinalDecisionDocs(data));
        docList.addAll(getCloseCaseDocuments(data));
        docList.addAll(getHearingSummaryDocuments(data));
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllCaseDocumentsExcludingInitialCicaUpload(final CaseData data) {
        return buildListValues(prepareListExcludingInitialCicaUpload(data));
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllCaseDocuments(final CaseData data) {
        return buildListValues(prepareList(data));
    }

    public static DynamicList prepareCICDocumentListWithAllDocuments(final CaseData data) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();

        dynamicListElements.addAll(getDynamicListElements(getCaseDocs(data.getCicCase()), CASE_TYPE));
        dynamicListElements.addAll(getDynamicListElements(getReinstateDocuments(data.getCicCase()), REINSTATE_TYPE));
        dynamicListElements.addAll(getDynamicListElements(getDocumentManagementDocs(data), DOC_MGMT_TYPE));
        dynamicListElements.addAll(getDynamicListElements(getCloseCaseDocuments(data), CLOSE_CASE_TYPE));
        dynamicListElements.addAll(getDynamicListElements(getHearingSummaryDocuments(data), HEARING_SUMMARY_TYPE));

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }

    private static List<DynamicListElement> getDynamicListElements(List<CaseworkerCICDocument> docList, String fileType) {
        return docList
            .stream()
            .filter(CaseworkerCICDocument::isDocumentValid)
            .map(doc -> DynamicListElement.builder()
                .label(fileType + DOUBLE_HYPHEN + doc.getDocumentLink().getFilename()
                    + DOUBLE_HYPHEN + doc.getDocumentCategory().getLabel()).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());
    }

    public static DynamicMultiSelectList prepareDocumentList(final CaseData data) {
        List<CaseworkerCICDocument> docList = prepareList(data);

        List<DynamicListElement> dynamicListElements = docList
            .stream()
            .filter(CaseworkerCICDocument::isDocumentValid)
            .map(doc ->
                DynamicListElement.builder()
                    .label(doc.getDocumentCategory().getLabel() + "--" + doc.getDocumentLink().getFilename())
                    .code(UUID.randomUUID()).build())
            .toList();

        return DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .value(new ArrayList<>())
            .build();
    }

    public static DynamicMultiSelectList prepareDocumentList(final CaseData data, String baseUrl) {
        List<CaseworkerCICDocument> docList = prepareList(data);
        String apiUrl = baseUrl + DOCUMENT_BINARY_PATH;
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        for (CaseworkerCICDocument doc : docList) {
            String documentId = StringUtils.substringAfterLast(doc.getDocumentLink().getUrl(),
                "/");
            String url = String.format(apiUrl, documentId);
            DynamicListElement element = DynamicListElement.builder().label("[" + doc.getDocumentLink().getFilename()
                + " " + doc.getDocumentCategory().getLabel()
                + "](" + url + ")").code(UUID.randomUUID()).build();
            dynamicListElements.add(element);
        }

        return DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .value(new ArrayList<>())
            .build();
    }

    public static DynamicMultiSelectList prepareContactPartiesDocumentList(final CaseData data, String baseUrl) {
        List<CaseworkerCICDocument> docList = prepareList(data);


        String apiUrl = baseUrl + "/documents/%s/binary";
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        for (CaseworkerCICDocument doc : docList) {
            String fileName = doc.getDocumentLink().getFilename();
            String fileExtension = StringUtils.substringAfterLast(fileName, ".");
            if (ContactPartiesAllowedFileTypes.isFileTypeValid(fileExtension)) {
                createDocumentList(apiUrl, dynamicListElements, doc);
            }
        }

        return DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .value(new ArrayList<>())
            .build();
    }


    private static void createDocumentList(String apiUrl, List<DynamicListElement> dynamicListElements, CaseworkerCICDocument doc) {
        String documentId = StringUtils.substringAfterLast(doc.getDocumentLink().getUrl(),
            "/");
        String url = String.format(apiUrl, documentId);
        DynamicListElement element = DynamicListElement.builder().label("[" + doc.getDocumentLink().getFilename()
            + " " + doc.getDocumentCategory().getLabel()
            + "](" + url + ")").code(UUID.randomUUID()).build();
        dynamicListElements.add(element);
    }


    private static List<CaseworkerCICDocument> getReinstateDocuments(CicCase cicCase) {
        List<CaseworkerCICDocument> reinstateDocList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getReinstateDocuments())) {
            for (ListValue<CaseworkerCICDocument> document : cicCase.getReinstateDocuments()) {
                reinstateDocList.add(document.getValue());
            }
        }
        return reinstateDocList;
    }

    private static List<CaseworkerCICDocument> getCaseDocs(CicCase cicCase) {
        List<CaseworkerCICDocument> caseDocs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getApplicantDocumentsUploaded())) {
            for (ListValue<CaseworkerCICDocument> document : cicCase.getApplicantDocumentsUploaded()) {
                caseDocs.add(document.getValue());
            }
        }
        return caseDocs;
    }

    private static List<CaseworkerCICDocument> getDocumentManagementDocs(CaseData caseData) {
        List<CaseworkerCICDocument> docManagementDocs = new ArrayList<>();
        if (null != caseData.getAllDocManagement() && !CollectionUtils.isEmpty(caseData.getAllDocManagement().getCaseworkerCICDocument())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getAllDocManagement().getCaseworkerCICDocument()) {
                docManagementDocs.add(document.getValue());
            }
        }
        return docManagementDocs;
    }

    private static List<CaseworkerCICDocument> getCloseCaseDocuments(CaseData caseData) {
        List<CaseworkerCICDocument> closeCaseDocs = new ArrayList<>();
        if (null != caseData.getCloseCase() && !CollectionUtils.isEmpty(caseData.getCloseCase().getDocuments())) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getCloseCase().getDocuments()) {
                closeCaseDocs.add(document.getValue());
            }
        }
        return closeCaseDocs;
    }

    private static List<CaseworkerCICDocument> getHearingSummaryDocuments(CaseData caseData) {
        List<CaseworkerCICDocument> hearingSummaryDocs = new ArrayList<>();

        Stream.ofNullable(caseData.getHearingList())
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(hearing -> !isNull(hearing.getSummary()) && !isEmpty(hearing.getSummary().getRecFile()))
            .map(Listing::getSummary)
            .map(HearingSummary::getRecFile)
            .flatMap(Collection::stream)
            .forEach(recFile -> hearingSummaryDocs.add(recFile.getValue()));

        return hearingSummaryDocs;
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllDecisionDocuments(CaseData caseData) {
        return buildListValues(getDecisionDocs(caseData));
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllFinalDecisionDocuments(CaseData caseData) {
        return buildListValues(getFinalDecisionDocs(caseData));
    }

    public static List<ListValue<CaseworkerCICDocument>> getAllOrderDocuments(CicCase cicCase) {
        return buildListValues(getOrderDocuments(cicCase));
    }

    public static Optional<CaseworkerCICDocument> getCaseDocumentById(String id, CaseData caseData) {
        var allDocuments = getAllCaseDocuments(caseData)
            .stream().map(ListValue::getValue)
            .toList();
        return allDocuments.stream().filter(document -> document.getDocumentLink().getBinaryUrl().contains(id)).findFirst();
    }

    public static List<String> extractDocumentIds(List<DynamicListElement> elements) {
        if (elements == null || elements.isEmpty()) {
            return List.of();
        }
        return elements.stream()
            .map(DocumentListUtil::extractDocumentId)
            .flatMap(Optional::stream)
            .toList();
    }

    private static Optional<String> extractDocumentId(DynamicListElement element) {
        if (element == null || element.getLabel() == null) {
            return Optional.empty();
        }

        String label = element.getLabel();
        int open = label.lastIndexOf('(');
        int close = label.lastIndexOf(')');

        if (open < 0 || close <= open) {
            return Optional.empty();
        }

        String url = label.substring(open + 1, close).trim();
        if (Strings.CS.endsWith(url, "/binary")) {
            url = org.apache.commons.lang3.StringUtils.substringBeforeLast(url, "/");
        }

        String documentId = org.apache.commons.lang3.StringUtils.substringAfterLast(url, "/");
        return org.apache.commons.lang3.StringUtils.isEmpty(documentId) ? Optional.empty() : Optional.of(documentId);
    }
}
