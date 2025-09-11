package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.sptribs.caseworker.event.page.UploadCaseDocuments;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.ByteArrayMultipartFile;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.pdf.PdfConversionService;
import uk.gov.hmcts.sptribs.document.pdf.PdfWatermarkService;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.convertToCaseworkerCICDocumentUpload;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.mapCdamDocumentToCicCaseworkerDocument;

@Component
@Slf4j
@RequiredArgsConstructor
public class RespondentDocumentManagement implements CCDConfig<CaseData, State, UserRole> {

    private final UploadCaseDocuments uploadCaseDocuments = new UploadCaseDocuments();
    private final PdfWatermarkService pdfWatermarkService;
    private final PdfConversionService pdfConversionService;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(RESPONDENT_DOCUMENT_MANAGEMENT)
                .forStates(Withdrawn,
                    Rejected,
                    Submitted,
                    NewCaseReceived,
                    CaseManagement,
                    ReadyToList,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Document management: Upload")
                .description("Document management: Upload")
                .showSummary()
                .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_RESPONDENT, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(
                    ST_CIC_CASEWORKER,
                    ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_SENIOR_JUDGE,
                    ST_CIC_JUDGE)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        uploadCaseDocuments.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = caseData.getNewDocManagement().getCaseworkerCICDocumentUpload();
        List<ListValue<CaseworkerCICDocument>> documents = convertToCaseworkerCICDocumentUpload(uploadedDocuments, false);
        caseData.getNewDocManagement().setCaseworkerCICDocumentUpload(new ArrayList<>());
        var convertedDocuments = uploadDocuments(documents, details);
        caseData.getNewDocManagement().setCaseworkerCICDocument(convertedDocuments);
        caseData.getNewDocManagement().setCaseworkerCICDocument(documents);


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }

    private List<ListValue<CaseworkerCICDocument>> uploadDocuments(List<ListValue<CaseworkerCICDocument>> documents,
                                                                   CaseDetails<CaseData, State> details) {
        var validBundleDocs = documents.stream()
            .filter(doc -> doc.getValue().isValidBundleDocument())
            .toList();

        int pageCount = details.getData().getCurrentBundlePageCount();

        var pdfConvertedDocs = pdfConversionService.convertFilesToPdf(validBundleDocs, String.valueOf(details.getId()));
        var watermarkedDocsAndUpdatedPageCount = pdfWatermarkService.addWatermarkToPdfs(pdfConvertedDocs, pageCount);

        var watermarkedDocs = watermarkedDocsAndUpdatedPageCount.getLeft();
        details.getData().setBundlePageCount(watermarkedDocsAndUpdatedPageCount.getRight());

        List<ListValue<CaseworkerCICDocument>> convertedDocs = new ArrayList<>();

        for (var watermarkedDoc : watermarkedDocs) {
            log.info("Document {} for case id {} converted to pdf and watermarked",
                watermarkedDoc.getFileName(), details.getId());

            MultipartFile multipartFile = ByteArrayMultipartFile.builder()
                .name("file")
                .content(watermarkedDoc.getFileContent())
                .originalName(watermarkedDoc.getFileName())
                .contentType(MediaType.APPLICATION_PDF)
                .build();

            final DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
                Classification.RESTRICTED.toString(),
                details.getCaseTypeId(),
                details.getJurisdiction(),
                List.of(multipartFile)
            );

            UploadResponse response = caseDocumentClientApi.uploadDocuments(
                idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
                authTokenGenerator.generate(),
                documentUploadRequest
            );

            log.info("Upload doc Response {} for case id {}", response, details.getId());

            convertedDocs.add(ListValue.<CaseworkerCICDocument>builder()
                .id(UUID.randomUUID().toString())
                .value(mapCdamDocumentToCicCaseworkerDocument(response.getDocuments().getFirst(),
                    watermarkedDoc.getOriginalDocument(), false))
                .build());
        }

        return convertedDocs;
    }
}
