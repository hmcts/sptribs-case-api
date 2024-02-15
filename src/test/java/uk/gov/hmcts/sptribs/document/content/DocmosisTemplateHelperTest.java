package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;
import uk.gov.hmcts.sptribs.ciccase.model.SchemeCic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_NUMBER;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CIC_CASE_SCHEME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.COMMA_SPACE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DECISION_DATED;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DIRECTION_DATED;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getCommonFields;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getMembers;

@ExtendWith(MockitoExtension.class)
class DocmosisTemplateHelperTest {

    private static final Long ccdCaseReference = 1649246783609781L;
    private static final String PANEL_MEMBER_1 = "Hubert Blaine Earl Frederick Gerald Irvin John Kenneth Charles David";
    private static final String PANEL_MEMBER_2 = "Charles David Earl Frederick Gerald Hubert Irvin John Kenneth Hubert Blaine";

    @Test
    void shouldReturnEmptyStringWhenMembersListIsNull() {
        final String members = getMembers(null);

        assertTrue(members.isEmpty());
    }

    @Test
    void shouldReturnEmptyStringWhenMembersListIsEmpty() {
        //When
        String members = getMembers(new ArrayList<>());

        //Then
        assertTrue(members.isEmpty());
    }

    @Test
    void shouldReturnFullMembersWhenMembersListSupplied() {
        //Given
        List<ListValue<PanelMember>> memberList = getPanelMembers();

        //When
        String members = getMembers(memberList);

        //Then
        assertEquals(PANEL_MEMBER_1 + COMMA_SPACE + PANEL_MEMBER_2, members);
    }

    @Test
    void shouldReturnFullMembersWhenMemberNull() {
        //Given
        List<ListValue<PanelMember>> memberList = new ArrayList<>();
        memberList.add(null);

        //When
        String members = getMembers(memberList);

        //Then
        assertTrue(members.isEmpty());
    }

    @Test
    void shouldReturnFullMembersWhenPanelMembersListNull() {
        //Given
        ListValue<PanelMember> member1 = new ListValue<>();
        List<ListValue<PanelMember>> memberList = new ArrayList<>();
        member1.setValue(null);
        memberList.add(member1);

        //When
        String members = getMembers(memberList);

        //Then
        assertTrue(members.isEmpty());
    }

    @Test
    void shouldReplaceCommonFieldsInTemplate() {
        //Given
        CaseData caseData = getCaseDataWithCICScheme(SchemeCic.Year1996);

        //When
        Map<String, Object> templateContent = getCommonFields(caseData, ccdCaseReference);

        //Then
        assertNotNull(templateContent.get(DECISION_DATED));
        assertNotNull(templateContent.get(DIRECTION_DATED));
        assertEquals(ccdCaseReference, templateContent.get(CASE_NUMBER));
        assertEquals(SchemeCic.Year1996.getLabel(), templateContent.get(CIC_CASE_SCHEME));
    }

    @Test
    void shouldReplaceCommonFieldsInTemplateWithNoCICCase() {
        //Given
        CaseData caseData = CaseData
            .builder()
            .cicCase(null)
            .build();

        //When
        Map<String, Object> templateContent = getCommonFields(caseData, ccdCaseReference);

        //Then
        assertNotNull(templateContent.get(DECISION_DATED));
        assertNotNull(templateContent.get(DIRECTION_DATED));
        assertEquals(ccdCaseReference, templateContent.get(CASE_NUMBER));
        assertNull(templateContent.get(CIC_CASE_SCHEME));
    }

    @Test
    void shouldReplaceCommonFieldsInTemplateWithNoCICScheme() {
        //Given
        CaseData caseData = getCaseDataWithCICScheme(null);

        //When
        Map<String, Object> templateContent = getCommonFields(caseData, ccdCaseReference);

        //Then
        assertNotNull(templateContent.get(DECISION_DATED));
        assertNotNull(templateContent.get(DIRECTION_DATED));
        assertEquals(ccdCaseReference, templateContent.get(CASE_NUMBER));
        assertNull(templateContent.get(CIC_CASE_SCHEME));
    }

    private List<ListValue<PanelMember>> getPanelMembers() {
        List<ListValue<PanelMember>> members = new ArrayList<>();
        ListValue<PanelMember> member1 = new ListValue<>();
        ListValue<PanelMember> member2 = new ListValue<>();

        PanelMember panelMember1 = PanelMember.builder()
            .name(getDynamicList(PANEL_MEMBER_1))
            .build();
        PanelMember panelMember2 = PanelMember.builder()
            .name(getDynamicList(PANEL_MEMBER_2))
            .build();

        member1.setValue(panelMember1);
        member2.setValue(panelMember2);
        members.add(member1);
        members.add(member2);
        return members;
    }

    private DynamicList getDynamicList(String panelMemberName) {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(panelMemberName)
            .code(UUID.randomUUID())
            .build();

        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private CaseData getCaseDataWithCICScheme(SchemeCic schemeCic) {
        return CaseData
            .builder()
            .cicCase(
                CicCase
                .builder()
                .schemeCic(schemeCic)
                .build())
            .build();
    }

}
