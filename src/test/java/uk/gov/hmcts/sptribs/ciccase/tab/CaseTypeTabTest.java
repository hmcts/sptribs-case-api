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
        final Tab.TabBuilder<CaseData, UserRole> summaryTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> stateTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> notesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseDetailsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> casePartiesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> ordersTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseDocsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> correspondenceTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> hearingsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> cicaDetailsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseCategoryTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> bundlingTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> messagesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseReferralTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseFlagTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseLinkTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);

        when(configBuilder.tab("summary", "Summary")).thenReturn(summaryTabBuilder);
        when(configBuilder.tab("state", "State")).thenReturn(stateTabBuilder);
        when(configBuilder.tab("notes", "Notes")).thenReturn(notesTabBuilder);
        when(configBuilder.tab("caseDetails", "Case Details")).thenReturn(caseDetailsTabBuilder);
        when(configBuilder.tab("caseParties", "Case Parties")).thenReturn(casePartiesTabBuilder);
        when(configBuilder.tab("orders", "Orders & Decisions")).thenReturn(ordersTabBuilder);
        when(configBuilder.tab("caseDocuments", "Case Documents")).thenReturn(caseDocsTabBuilder);
        when(configBuilder.tab("correspondence", "Correspondence")).thenReturn(correspondenceTabBuilder);
        when(configBuilder.tab("hearings", "Hearings")).thenReturn(hearingsTabBuilder);
        when(configBuilder.tab("cicaDetails", "CICA Details")).thenReturn(cicaDetailsTabBuilder);
        when(configBuilder.tab("caseFileView", "Case file view")).thenReturn(caseCategoryTabBuilder);
        when(configBuilder.tab("bundles", "Bundles")).thenReturn(bundlingTabBuilder);
        when(configBuilder.tab("messages", "Messages")).thenReturn(messagesTabBuilder);
        when(configBuilder.tab("caseReferrals", "Case Referrals")).thenReturn(caseReferralTabBuilder);
        when(configBuilder.tab("caseFlags", "Case Flags")).thenReturn(caseFlagTabBuilder);
        when(configBuilder.tab("caseLinks", "Linked cases")).thenReturn(caseLinkTabBuilder);

        //When
        caseTypeTab.configure(configBuilder);
        final Tab<CaseData, UserRole> summaryTab = summaryTabBuilder.build();
        final Tab<CaseData, UserRole> caseDetailsTab = caseDetailsTabBuilder.build();
        final Tab<CaseData, UserRole> casePartiesTab = casePartiesTabBuilder.build();
        final Tab<CaseData, UserRole> ordersTab = ordersTabBuilder.build();
        final Tab<CaseData, UserRole> caseDocsTab = caseDocsTabBuilder.build();
        final Tab<CaseData, UserRole> hearingsTab = hearingsTabBuilder.build();
        final Tab<CaseData, UserRole> cicaDetailsTab = cicaDetailsTabBuilder.build();
        final Tab<CaseData, UserRole> caseCategoryTab = caseCategoryTabBuilder.build();
        final Tab<CaseData, UserRole> messages = messagesTabBuilder.build();
        final Tab<CaseData, UserRole> bundlingTab = bundlingTabBuilder.build();
        final Tab<CaseData, UserRole> caseReferralTab = caseReferralTabBuilder.build();
        final Tab<CaseData, UserRole> caseFlagsTab = caseFlagTabBuilder.build();
        final Tab<CaseData, UserRole> caseLinkTab = caseLinkTabBuilder.build();

        //Then
        assertThat(summaryTab.getFields()).extracting(TabField::getId).contains("cicCaseFullName");
        assertThat(caseDetailsTab.getFields()).extracting(TabField::getId).contains("cicCaseCaseCategory");
        assertThat(casePartiesTab.getFields()).extracting(TabField::getId).contains("cicCaseFullName");
        assertThat(ordersTab.getFields()).extracting(TabField::getId).contains("cicCaseOrderList");
        assertThat(caseDocsTab.getFields()).extracting(TabField::getId).contains("cicCaseApplicantDocumentsUploaded");
        assertThat(hearingsTab.getFields()).extracting(TabField::getId).contains("hearingStatus");
        assertThat(cicaDetailsTab.getFields()).extracting(TabField::getId).contains("CICA Details");
        assertThat(caseCategoryTab.getFields()).extracting(TabField::getDisplayContextParameter).isNotNull();
        assertThat(messages.getFields()).extracting(TabField::getId).isNotNull();
        assertThat(bundlingTab.getFields()).extracting(TabField::getId).isNotNull();
        assertThat(caseReferralTab.getFields()).extracting(TabField::getId).contains("Referral to Judge");
        assertThat(caseReferralTab.getFields()).extracting(TabField::getId).contains("Referral to Legal Officer");
    }

    @Test
    void shouldConfigureCorrespondenceTab() {
        //Given
        final Tab.TabBuilder<CaseData, UserRole> summaryTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> stateTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> notesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseDetailsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> casePartiesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> ordersTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseDocsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> correspondenceTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> hearingsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> cicaDetailsTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseCategoryTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> bundlingTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> messagesTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseReferralTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseFlagTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);
        final Tab.TabBuilder<CaseData, UserRole> caseLinkTabBuilder = Tab.TabBuilder.builder(CaseData.class, utils);

        when(configBuilder.tab("summary", "Summary")).thenReturn(summaryTabBuilder);
        when(configBuilder.tab("state", "State")).thenReturn(stateTabBuilder);
        when(configBuilder.tab("notes", "Notes")).thenReturn(notesTabBuilder);
        when(configBuilder.tab("caseDetails", "Case Details")).thenReturn(caseDetailsTabBuilder);
        when(configBuilder.tab("caseParties", "Case Parties")).thenReturn(casePartiesTabBuilder);
        when(configBuilder.tab("orders", "Orders & Decisions")).thenReturn(ordersTabBuilder);
        when(configBuilder.tab("caseDocuments", "Case Documents")).thenReturn(caseDocsTabBuilder);
        when(configBuilder.tab("correspondence", "Correspondence")).thenReturn(correspondenceTabBuilder);
        when(configBuilder.tab("hearings", "Hearings")).thenReturn(hearingsTabBuilder);
        when(configBuilder.tab("cicaDetails", "CICA Details")).thenReturn(cicaDetailsTabBuilder);
        when(configBuilder.tab("caseFileView", "Case file view")).thenReturn(caseCategoryTabBuilder);
        when(configBuilder.tab("bundles", "Bundles")).thenReturn(bundlingTabBuilder);
        when(configBuilder.tab("messages", "Messages")).thenReturn(messagesTabBuilder);
        when(configBuilder.tab("caseReferrals", "Case Referrals")).thenReturn(caseReferralTabBuilder);
        when(configBuilder.tab("caseFlags", "Case Flags")).thenReturn(caseFlagTabBuilder);
        when(configBuilder.tab("caseLinks", "Linked cases")).thenReturn(caseLinkTabBuilder);

        //When
        caseTypeTab.configure(configBuilder);
        final Tab<CaseData, UserRole> correspondenceTab = correspondenceTabBuilder.build();

        //Then
        assertThat(correspondenceTab.getFields()).extracting(TabField::getId).isNotNull();
    }
}
