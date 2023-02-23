package uk.gov.hmcts.sptribs.ciccase.tab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.PropertyUtils;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.ccd.sdk.api.TabField;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseTypeTabTest {

    @InjectMocks
    private CaseTypeTab caseTypeTab;

    @Mock
    private ConfigBuilderImpl<CaseData, State, UserRole> configBuilder;

    @Mock
    private PropertyUtils utils;

    @Test
    void shouldConfigureCaseTypeTab() {
        //Given
        Tab.TabBuilder<CaseData, UserRole> summaryTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> flagsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> stateTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> notesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> caseDetailsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> casePartiesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> ordersTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> caseDocsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        Tab.TabBuilder<CaseData, UserRole> hearingsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);

        when(configBuilder.tab("summary", "Summary")).thenReturn(summaryTabBuilder);
        when(configBuilder.tab("flags", "Flags")).thenReturn(flagsTabBuilder);
        when(configBuilder.tab("state", "State")).thenReturn(stateTabBuilder);
        when(configBuilder.tab("notes", "Notes")).thenReturn(notesTabBuilder);
        when(configBuilder.tab("caseDetails", "Case Details")).thenReturn(caseDetailsTabBuilder);
        when(configBuilder.tab("caseParties", "Case Parties")).thenReturn(casePartiesTabBuilder);
        when(configBuilder.tab("orders", "Orders")).thenReturn(ordersTabBuilder);
        when(configBuilder.tab("caseDocuments", "Case Documents")).thenReturn(caseDocsTabBuilder);
        when(configBuilder.tab("hearings", "Hearings")).thenReturn(hearingsTabBuilder);

        //When
        caseTypeTab.configure(configBuilder);
        Tab<CaseData, UserRole> summaryTab = summaryTabBuilder.build();
        Tab<CaseData, UserRole> flagsTab = flagsTabBuilder.build();
        Tab<CaseData, UserRole> caseDetailsTab = caseDetailsTabBuilder.build();
        Tab<CaseData, UserRole> casePartiesTab = casePartiesTabBuilder.build();
        Tab<CaseData, UserRole> ordersTab = ordersTabBuilder.build();
        Tab<CaseData, UserRole> caseDocsTab = caseDocsTabBuilder.build();
        Tab<CaseData, UserRole> hearingsTab = hearingsTabBuilder.build();

        //Then
        assertThat(summaryTab.getFields()).extracting(TabField::getId).contains("cicCaseFullName");
        assertThat(flagsTab.getFields()).extracting(TabField::getId).contains("caseFlagPartyLevelFlags");
        assertThat(caseDetailsTab.getFields()).extracting(TabField::getId).contains("cicCaseCaseCategory");
        assertThat(casePartiesTab.getFields()).extracting(TabField::getId).contains("cicCaseFullName");
        assertThat(ordersTab.getFields()).extracting(TabField::getId).contains("cicCaseOrderList");
        assertThat(caseDocsTab.getFields()).extracting(TabField::getId).contains("cicCaseApplicantDocumentsUploaded");
        assertThat(hearingsTab.getFields()).extracting(TabField::getId).contains("cicCaseHearingNotificationParties");
    }
}
