package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.links.LinkService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @InjectMocks
    private LinkService linkService;

    @Test
    void shouldPopulateLinkDynamicList() {
        CaseData data = caseData();
        CaseLink links = CaseLink.builder().caseReference("1").build();
        ListValue<CaseLink> caseLinksListValue = new ListValue<>();
        caseLinksListValue.setValue(links);
        List<ListValue<CaseLink>> caseLinks = new ArrayList<>();
        caseLinks.add(caseLinksListValue);
        data.setCaseLinks(caseLinks);

        //When
        DynamicMultiSelectList result = linkService.prepareLinkList(data);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getListItems()).hasSize(1);
    }

    @Test
    void shouldRemoveLink() {
        CaseLink links = CaseLink.builder().caseReference("0").build();
        ListValue<CaseLink> caseLinksListValue = new ListValue<>();
        caseLinksListValue.setValue(links);
        CaseLink links2 = CaseLink.builder().caseReference("1").build();
        ListValue<CaseLink> caseLinksListValue2 = new ListValue<>();
        caseLinksListValue2.setValue(links2);
        CaseLink links3 = CaseLink.builder().caseReference("2").build();
        ListValue<CaseLink> caseLinksListValue3 = new ListValue<>();
        caseLinksListValue3.setValue(links3);
        List<ListValue<CaseLink>> caseLinks = new ArrayList<>();
        caseLinks.add(caseLinksListValue);
        caseLinks.add(caseLinksListValue2);
        caseLinks.add(caseLinksListValue3);

        CaseData data = caseData();
        data.setCaseLinks(caseLinks);
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("0")
            .code(UUID.randomUUID())
            .build();
        CicCase cicCase = CicCase.builder().linkDynamicList(DynamicMultiSelectList
            .builder()
            .value(List.of(listItem))
            .listItems(List.of(listItem))
            .build()).build();
        data.setCicCase(cicCase);
        //When
        List<ListValue<CaseLink>> result = linkService.removeLinks(data);

        //Then
        assertThat(result).hasSize(2);
    }

}
