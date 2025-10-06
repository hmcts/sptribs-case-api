package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingService;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.Callback;
import uk.gov.hmcts.sptribs.document.model.AbstractCaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.getAllCaseDocumentsExcludingInitialCicaUpload;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerCreateBundle implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    BundlingService bundlingService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CREATE_BUNDLE)
                .forStates(CaseManagement, AwaitingHearing)
                .name("Bundle: Create a bundle")
                .description("Bundle: Create a bundle")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE)
                .publishToCamunda();

        new PageBuilder(eventBuilder)
                .page("createBundle")
                .pageLabel("Create a bundle")
                .done();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final List<ListValue<CaseworkerCICDocument>> documentListValues = DocumentListUtil.getAllCaseDocuments(caseData);
        final List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> abstractCaseworkerCICDocumentList = new ArrayList<>();

        if (caseData.isBundleOrderEnabled()) {
            log.info("Preparing documents for bundle creation for case id: {} - new bundle enabled", details.getId());
            caseData.setCaseDocuments(getInitialCicaUpload(caseData));
            caseData.setFurtherCaseDocuments(getFurtherDocuments(caseData));
        } else {
            log.info("Preparing documents for bundle creation for case id: {} - new bundle not enabled", details.getId());
            prepareBundleDocumentsPreBundleChanges(documentListValues, abstractCaseworkerCICDocumentList, caseData);
        }

        caseData.setBundleConfiguration(bundlingService.getMultiBundleConfig());
        caseData.setMultiBundleConfiguration(bundlingService.getMultiBundleConfigs());
        caseData.setCaseNumber(String.valueOf(details.getId()));
        caseData.setSubjectRepFullName(caseData.getCicCase().getFullName());
        caseData.setSchemeLabel(caseData.getCicCase().getSchemeCic() != null ? caseData.getCicCase().getSchemeCic().getLabel() : "");
        details.setData(caseData);

        final Callback callback = new Callback(details, beforeDetails, CREATE_BUNDLE, true);
        final BundleCallback bundleCallback = new BundleCallback(callback);

        caseData.setCaseBundles(bundlingService.buildBundleListValues(bundlingService.createBundle(bundleCallback)));

        caseData.setMultiBundleConfiguration(null);
        caseData.setCaseDocuments(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private static void prepareBundleDocumentsPreBundleChanges(List<ListValue<CaseworkerCICDocument>> documentListValues,
                                                               List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> cicDocumentList,
                                                               CaseData caseData) {
        for (ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue : documentListValues) {
            CaseworkerCICDocument document = caseworkerCICDocumentListValue.getValue();
            if (document.isValidBundleDocument()) {
                cicDocumentList.add(new AbstractCaseworkerCICDocument<>(caseworkerCICDocumentListValue.getValue()));
            }
        }

        caseData.setCaseDocuments(cicDocumentList);
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> getInitialCicaUpload(CaseData caseData) {
        var initialDocs = caseData.getInitialCicaDocuments();

        List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> abstractCaseworkerCICDocumentList = new ArrayList<>();

        for (ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue : initialDocs) {
            CaseworkerCICDocument document = caseworkerCICDocumentListValue.getValue();
            if (document.isValidBundleDocument()) {
                abstractCaseworkerCICDocumentList.add(new AbstractCaseworkerCICDocument<>(caseworkerCICDocumentListValue.getValue()));
            }
        }

        return abstractCaseworkerCICDocumentList;
    }

    private List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> getFurtherDocuments(CaseData caseData) {
        var docs = getAllCaseDocumentsExcludingInitialCicaUpload(caseData);

        List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> abstractCaseworkerCICDocumentList = new ArrayList<>();

        for (ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue : docs) {
            CaseworkerCICDocument document = caseworkerCICDocumentListValue.getValue();
            if (document.isValidBundleDocument()) {
                abstractCaseworkerCICDocumentList.add(new AbstractCaseworkerCICDocument<>(caseworkerCICDocumentListValue.getValue()));
            }
        }

        abstractCaseworkerCICDocumentList.sort(
            Comparator.comparing(
                doc -> doc.getValue().getDate(),
                Comparator.nullsLast(Comparator.naturalOrder())
            )
        );
        return abstractCaseworkerCICDocumentList;
    }
}
