package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseLinks;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

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
        CaseLinks links = CaseLinks.builder().caseReference("1").build();
        ListValue<CaseLinks> caseLinksListValue = new ListValue<>();
        caseLinksListValue.setValue(links);
        List<ListValue<CaseLinks>> caseLinks = new ArrayList<>();
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
        CaseLinks links = CaseLinks.builder().caseReference("0").build();
        ListValue<CaseLinks> caseLinksListValue = new ListValue<>();
        caseLinksListValue.setValue(links);
        CaseLinks links2 = CaseLinks.builder().caseReference("1").build();
        ListValue<CaseLinks> caseLinksListValue2 = new ListValue<>();
        caseLinksListValue2.setValue(links2);
        CaseLinks links3 = CaseLinks.builder().caseReference("2").build();
        ListValue<CaseLinks> caseLinksListValue3 = new ListValue<>();
        caseLinksListValue3.setValue(links3);
        List<ListValue<CaseLinks>> caseLinks = new ArrayList<>();
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
        List<ListValue<CaseLinks>> result = linkService.removeLinks(data);

        //Then
        assertThat(result).hasSize(2);
    }

}
