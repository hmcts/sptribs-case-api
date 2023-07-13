package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseLinks;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkCaseSelectCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("linkCaseSelectCase", this::midEvent)
            .pageLabel("Select a case you want to link to this case")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getLinkCaseNumber)
            .mandatory(CicCase::getLinkCaseReason)
            .mandatory(CicCase::getLinkCaseOtherDescription, "cicCaseLinkCaseReason = \"other\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        CaseLinks caseLink = CaseLinks.builder()
            .caseReference(data.getCicCase().getLinkCaseNumber())
            .reasonForLink(Set.of(data.getCicCase().getLinkCaseReason()))
            .createdDateTime(null)
            .caseType("CriminalInjuriesCompensation")
            .build();

        if (CollectionUtils.isEmpty(data.getCaseLinks())) {
            List<ListValue<CaseLinks>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<CaseLinks>builder()
                .id("1")
                .value(caseLink)
                .build();

            listValues.add(listValue);

            data.setCaseLinks(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<CaseLinks>builder()
                .value(caseLink)
                .build();

            data.getCaseLinks().add(0, listValue); // always add new note as first element so that it is displayed on top

            data.getCaseLinks().forEach(
                caseLinkListValue -> caseLinkListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
