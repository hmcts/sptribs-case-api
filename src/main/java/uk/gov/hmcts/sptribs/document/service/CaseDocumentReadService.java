package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseDocumentProjectionMapper;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.model.BundleDocumentsView;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentReadPurpose;
import uk.gov.hmcts.sptribs.document.model.SelectedCaseDocuments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseDocumentReadService {

    private static final String DOCUMENT_BINARY_PATH = "documents/%s/binary";

    private final DocumentsRepository documentsRepository;
    private final CaseDocumentTypesCache caseDocumentTypesCache;
    private final CaseDocumentPolicy caseDocumentPolicy;
    private final CaseDocumentProjectionMapper projectionMapper;

    public List<ListValue<CaseDocumentView>> getCaseViewDocuments(long caseReference) {
        return getDocuments(caseReference, DocumentReadPurpose.CASE_VIEW).stream()
            .map(document -> projectionMapper.mapCaseView(document.entity(), document.type()))
            .toList();
    }

    public BundleDocumentsView getBundleDocuments(long caseReference, CaseData caseData) {
        List<TypedDocument> typedDocuments = getDocuments(caseReference, DocumentReadPurpose.BUNDLE);
        Set<String> initialBinaryUrls = getInitialBinaryUrls(caseData);

        List<CaseworkerCICDocument> allDocuments = typedDocuments.stream()
            .map(TypedDocument::entity)
            .map(projectionMapper::mapBundleOrSelection)
            .toList();

        List<CaseworkerCICDocument> initialDocuments = typedDocuments.stream()
            .map(TypedDocument::entity)
            .filter(entity -> initialBinaryUrls.contains(entity.getDocumentBinaryUrl()))
            .map(projectionMapper::mapBundleOrSelection)
            .toList();

        List<CaseworkerCICDocument> furtherDocuments = typedDocuments.stream()
            .map(TypedDocument::entity)
            .filter(entity -> !initialBinaryUrls.contains(entity.getDocumentBinaryUrl()))
            .map(projectionMapper::mapBundleOrSelection)
            .sorted(Comparator.comparing(
                CaseworkerCICDocument::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .toList();

        return BundleDocumentsView.builder()
            .allDocuments(allDocuments)
            .initialDocuments(initialDocuments)
            .furtherDocuments(furtherDocuments)
            .build();
    }

    public DynamicMultiSelectList getContactPartyOptions(long caseReference, String baseUrl) {
        String apiUrl = baseUrl.replaceAll("/$", "") + "/" + DOCUMENT_BINARY_PATH;

        List<DynamicListElement> options = getDocuments(caseReference, DocumentReadPurpose.CONTACT_PARTIES).stream()
            .map(TypedDocument::entity)
            .map(entity -> toContactPartyOption(entity, apiUrl))
            .flatMap(java.util.Optional::stream)
            .toList();

        return DynamicMultiSelectList.builder()
            .listItems(options)
            .value(new ArrayList<>())
            .build();
    }

    public SelectedCaseDocuments getSelectedContactPartyDocuments(
        long caseReference,
        DynamicMultiSelectList selection,
        int limit
    ) {
        Set<UUID> selectedIds = selectedIds(selection);
        if (selectedIds.isEmpty()) {
            return emptySelection();
        }

        Map<UUID, DocumentEntity> documentsById = new LinkedHashMap<>();
        getDocuments(caseReference, DocumentReadPurpose.CONTACT_PARTIES).stream()
            .map(TypedDocument::entity)
            .forEach(entity -> DocumentUtil.extractDocumentId(entity.getDocumentUrl())
                .ifPresent(id -> documentsById.put(id, entity)));

        List<DocumentEntity> selectedEntities = selectedIds.stream()
            .map(documentsById::get)
            .filter(java.util.Objects::nonNull)
            .limit(limit)
            .toList();

        return SelectedCaseDocuments.builder()
            .documentEntityIds(selectedEntities.stream().map(DocumentEntity::getId).toList())
            .documents(selectedEntities.stream().map(projectionMapper::mapBundleOrSelection).toList())
            .build();
    }

    private List<TypedDocument> getDocuments(long caseReference, DocumentReadPurpose purpose) {
        return documentsRepository.findAllByCaseReferenceNumberOrderBySavedAtDesc(caseReference).stream()
            .map(this::toTypedDocument)
            .flatMap(java.util.Optional::stream)
            .filter(document -> caseDocumentPolicy.includes(purpose, document.type(), document.entity()))
            .toList();
    }

    private java.util.Optional<TypedDocument> toTypedDocument(DocumentEntity entity) {
        try {
            return java.util.Optional.of(new TypedDocument(
                entity,
                caseDocumentTypesCache.getType(entity.getCaseDocumentTypeId())
            ));
        } catch (IllegalArgumentException exception) {
            log.warn("Ignoring document {} with unsupported case document type id {}",
                entity.getId(), entity.getCaseDocumentTypeId());
            return java.util.Optional.empty();
        }
    }

    private java.util.Optional<DynamicListElement> toContactPartyOption(DocumentEntity entity, String apiUrl) {
        return DocumentUtil.extractDocumentId(entity.getDocumentUrl())
            .map(documentId -> {
                CaseworkerCICDocument document = projectionMapper.mapBundleOrSelection(entity);
                String category = document.getDocumentCategory() == null
                    ? "Uncategorised"
                    : document.getDocumentCategory().getLabel();
                String label = "[" + entity.getDocumentFilename() + " " + category + "]("
                    + String.format(apiUrl, documentId) + ")";
                return DynamicListElement.builder().code(documentId).label(label).build();
            });
    }

    private Set<String> getInitialBinaryUrls(CaseData caseData) {
        if (caseData == null || caseData.getInitialCicaDocuments() == null) {
            return Set.of();
        }

        Set<String> binaryUrls = new LinkedHashSet<>();
        caseData.getInitialCicaDocuments().stream()
            .filter(java.util.Objects::nonNull)
            .map(ListValue::getValue)
            .filter(java.util.Objects::nonNull)
            .map(CaseworkerCICDocument::getDocumentLink)
            .filter(java.util.Objects::nonNull)
            .map(uk.gov.hmcts.ccd.sdk.type.Document::getBinaryUrl)
            .filter(java.util.Objects::nonNull)
            .forEach(binaryUrls::add);
        return binaryUrls;
    }

    private Set<UUID> selectedIds(DynamicMultiSelectList selection) {
        if (selection == null || selection.getValue() == null) {
            return Set.of();
        }

        Set<UUID> ids = new LinkedHashSet<>();
        selection.getValue().stream()
            .filter(java.util.Objects::nonNull)
            .map(DynamicListElement::getCode)
            .filter(java.util.Objects::nonNull)
            .forEach(ids::add);
        return ids;
    }

    private SelectedCaseDocuments emptySelection() {
        return SelectedCaseDocuments.builder()
            .documentEntityIds(List.of())
            .documents(List.of())
            .build();
    }

    private record TypedDocument(DocumentEntity entity, CaseDocumentType type) {
    }
}
