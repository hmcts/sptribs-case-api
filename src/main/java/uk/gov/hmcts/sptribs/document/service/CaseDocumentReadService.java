package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesAllowedFileTypes;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentLookupException;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.document.DocumentFileTypes;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.document.exception.DocumentSelectionException;
import uk.gov.hmcts.sptribs.document.model.BundleDocumentsView;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.SelectedCaseDocuments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseDocumentReadService {

    private static final String DOCUMENT_BINARY_PATH = "documents/%s/binary";

    private static final Set<CaseDocumentType> CASE_VIEW_TYPES = Set.of(
        CaseDocumentType.APPLICATION,
        CaseDocumentType.DOCUMENT_MANAGEMENT,
        CaseDocumentType.ORDER,
        CaseDocumentType.DRAFT_ORDER,
        CaseDocumentType.DECISION,
        CaseDocumentType.FINAL_DECISION,
        CaseDocumentType.HEARING_RECORD,
        CaseDocumentType.OTHER
    );

    private static final Set<CaseDocumentType> SELECTABLE_TYPES = Set.of(
        CaseDocumentType.APPLICATION,
        CaseDocumentType.DOCUMENT_MANAGEMENT,
        CaseDocumentType.ORDER,
        CaseDocumentType.DECISION,
        CaseDocumentType.FINAL_DECISION,
        CaseDocumentType.HEARING_RECORD,
        CaseDocumentType.OTHER
    );

    private final DocumentsRepository documentsRepository;
    private final CaseDocumentTypesCache caseDocumentTypesCache;
    private final CaseworkerCICDocumentMapper documentMapper;

    public List<ListValue<CaseDocumentView>> getCaseViewDocuments(long caseReference) {
        return getDocuments(caseReference, CASE_VIEW_TYPES).stream()
            .map(document -> documentMapper.mapCaseDocumentView(document.entity(), document.type()))
            .toList();
    }

    public BundleDocumentsView getBundleDocuments(
        long caseReference,
        Set<String> initialDocumentBinaryUrls
    ) {
        Set<String> initialBinaryUrls = initialDocumentBinaryUrls == null
            ? Set.of()
            : initialDocumentBinaryUrls;

        List<ProjectedDocument> projectedDocuments = getDocuments(caseReference, SELECTABLE_TYPES).stream()
            .map(TypedDocument::entity)
            .filter(this::isValidBundleDocument)
            .map(entity -> new ProjectedDocument(
                entity.getDocumentBinaryUrl(),
                documentMapper.mapDocument(entity)
            ))
            .toList();

        List<CaseworkerCICDocument> allDocuments = projectedDocuments.stream()
            .map(ProjectedDocument::document)
            .toList();

        List<CaseworkerCICDocument> initialDocuments = projectedDocuments.stream()
            .filter(document -> initialBinaryUrls.contains(document.binaryUrl()))
            .map(ProjectedDocument::document)
            .toList();

        List<CaseworkerCICDocument> furtherDocuments = projectedDocuments.stream()
            .filter(document -> !initialBinaryUrls.contains(document.binaryUrl()))
            .map(ProjectedDocument::document)
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
        String apiUrl = removeTrailingSlash(baseUrl) + "/" + DOCUMENT_BINARY_PATH;

        List<DynamicListElement> options = getDocuments(caseReference, SELECTABLE_TYPES).stream()
            .map(TypedDocument::entity)
            .filter(this::isValidContactPartyDocument)
            .map(entity -> toContactPartyOption(entity, apiUrl))
            .flatMap(Optional::stream)
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
        if (limit <= 0 || selectedIds.size() > limit) {
            throw new DocumentSelectionException(
                "Selected " + selectedIds.size() + " documents when the permitted limit is " + limit
            );
        }

        Set<Long> selectableTypeIds = getCaseDocumentTypesById(SELECTABLE_TYPES).keySet();
        List<DocumentEntity> selectedEntities = selectedIds.stream()
            .map(documentId -> getSelectedDocument(caseReference, documentId))
            .filter(entity -> selectableTypeIds.contains(entity.getCaseDocumentTypeId()))
            .filter(this::isValidContactPartyDocument)
            .toList();

        if (selectedEntities.size() != selectedIds.size()) {
            throw new DocumentSelectionException(
                "One or more selected documents are not available for case " + caseReference
            );
        }

        return SelectedCaseDocuments.builder()
            .documentEntityIds(selectedEntities.stream().map(DocumentEntity::getId).toList())
            .documents(selectedEntities.stream().map(documentMapper::mapDocument).toList())
            .build();
    }

    private DocumentEntity getSelectedDocument(long caseReference, UUID documentId) {
        try {
            return documentsRepository.findByCaseReferenceAndDocumentIdUuid(caseReference, documentId.toString())
                .orElseThrow(() -> new DocumentSelectionException(
                    "Selected document " + documentId + " is not available for case " + caseReference
                ));
        } catch (DataAccessException e) {
            throw new DocumentLookupException(
                "Error loading selected document " + documentId + " for case " + caseReference,
                e
            );
        }
    }

    private List<TypedDocument> getDocuments(long caseReference, Set<CaseDocumentType> documentTypes) {
        Map<Long, CaseDocumentType> documentTypesById = getCaseDocumentTypesById(documentTypes);

        return documentsRepository.findDocumentsByReferenceAndCaseDocumentTypeIds(
            caseReference,
            List.copyOf(documentTypesById.keySet())
        ).stream()
            .filter(this::hasDocumentUrl)
            .map(entity -> new TypedDocument(entity, documentTypesById.get(entity.getCaseDocumentTypeId())))
            .filter(document -> document.type() != null)
            .toList();
    }

    private Map<Long, CaseDocumentType> getCaseDocumentTypesById(Set<CaseDocumentType> documentTypes) {
        return documentTypes.stream()
            .collect(Collectors.toMap(caseDocumentTypesCache::getId, Function.identity()));
    }

    private boolean hasDocumentUrl(DocumentEntity document) {
        return document != null && StringUtils.isNotBlank(document.getDocumentUrl());
    }

    private boolean isValidBundleDocument(DocumentEntity document) {
        return hasDocumentUrl(document)
            && DocumentFileTypes.isValid(
                document.getDocumentFilename(),
                DocumentFileTypes.BUNDLE_DOCUMENT_EXTENSIONS
            );
    }

    private boolean isValidContactPartyDocument(DocumentEntity document) {
        return hasDocumentUrl(document)
            && ContactPartiesAllowedFileTypes.isFileTypeValid(
                StringUtils.substringAfterLast(document.getDocumentFilename(), ".")
            );
    }

    private Optional<DynamicListElement> toContactPartyOption(DocumentEntity entity, String apiUrl) {
        return DocumentUtil.extractDocumentId(entity.getDocumentUrl())
            .map(documentId -> {
                CaseworkerCICDocument document = documentMapper.mapDocument(entity);
                String category = document.getDocumentCategory() == null
                    ? "Uncategorised"
                    : document.getDocumentCategory().getLabel();
                String label = String.format(
                    "[%s %s](%s)",
                    entity.getDocumentFilename(),
                    category,
                    String.format(apiUrl, documentId)
                );
                return DynamicListElement.builder().code(documentId).label(label).build();
            });
    }

    private String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
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

    private record ProjectedDocument(String binaryUrl, CaseworkerCICDocument document) {
    }
}
