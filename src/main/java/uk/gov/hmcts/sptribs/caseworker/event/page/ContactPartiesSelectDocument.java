package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContactPartiesSelectDocument implements CcdPageConfiguration {

    private static final int MAX_DOCUMENT_COUNT = 10;
    private static final long TWO_MEGABYTES = 2_048_000;

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("contactPartiesSelectDocument", this::midEvent)
            .pageLabel("Documents to include")
            .label("LabelContactPartiesSelectDocument", "Select a case document to include with this message(Optional)")
            .label("LabelContactPartiesSelectDocumentNote",
                "Note: Gov.Notify only supports sending documents in the formats of PDF, CSV, txt, rtf, MS Word Document "
                    + "file and MS Excel File. Your file must be smaller than 2MB")
            .complex(CaseData::getContactPartiesDocuments)
            .optionalWithLabel(ContactPartiesDocuments::getDocumentList,"Selected Documents")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        DynamicMultiSelectList list = data.getContactPartiesDocuments().getDocumentList();
        if (list != null && list.getValue() != null && list.getValue().size() > MAX_DOCUMENT_COUNT) {
            errors.add("Select up to 10 documents");
        }

        if (list != null && list.getValue() != null) {
            validateDocumentFileSizes(list.getValue(), errors);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private void validateDocumentFileSizes(List<DynamicListElement> userSelection, List<String> errors) {
        var docIds = DocumentListUtil.extractDocumentIds(userSelection);

        var systemUserAuth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        var serviceAuth = authTokenGenerator.generate();

        var docIdsAndSizes = docIds.stream()
            .map(id -> {
                var response = caseDocumentClientApi.getDocument(systemUserAuth, serviceAuth, UUID.fromString(id));
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Failed to retrieve document with id " + id);
                }
                return response.getBody() != null
                    ? Pair.of(id, response.getBody().size)
                    : Pair.of(id, TWO_MEGABYTES * 2);
            }).toList();

        for (var docIdAndSize : docIdsAndSizes) {
            if (docIdAndSize.getRight() > TWO_MEGABYTES) {
                userSelection.forEach(element -> {
                    if (element.getLabel().contains(docIdAndSize.getLeft())) {
                        String documentName = extractDocumentDisplayName(element.getLabel());
                        errors.add("Unable to proceed because " + documentName + " is larger than 2MB");
                    }
                });
            }
        }
    }

    private String extractDocumentDisplayName(String label) {
        if (StringUtils.isBlank(label)) {
            return label;
        }

        int start = label.indexOf('[');
        int end = label.indexOf(']', start + 1);

        if (start >= 0 && end > start) {
            return label.substring(start + 1, end);
        }

        return label;
    }

}
