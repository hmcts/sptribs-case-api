package uk.gov.hmcts.sptribs.testutils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static uk.gov.hmcts.sptribs.e2e.Base.CASE_API_BASE_URL;

public class PageHelpers {
    private PageHelpers() {
    }

    public static Page.WaitForSelectorOptions selectorOptionsWithTimeout(int timeout) {
        return new Page.WaitForSelectorOptions().setTimeout(timeout);
    }

    public static Page.WaitForLoadStateOptions loadStateOptionsWithTimeout(int timeout) {
        return new Page.WaitForLoadStateOptions().setTimeout(timeout);
    }

    public static Page.SelectOptionOptions selectOptionWithTimeout(int timeout) {
        return new Page.SelectOptionOptions().setTimeout(timeout);
    }

    public static Page.WaitForURLOptions urlOptionsWithTimeout(int timeout) {
        return new Page.WaitForURLOptions().setTimeout(timeout);
    }

    public static Page.WaitForFunctionOptions functionOptionsWithTimeout(int timeout) {
        return new Page.WaitForFunctionOptions().setTimeout(timeout);
    }

    public static Locator.ClickOptions clickOptionsWithTimeout(int timeout) {
        return new Locator.ClickOptions().setTimeout(timeout);
    }

    public static Page.GetByRoleOptions getRoleOptions(String buttonText) {
        return new Page.GetByRoleOptions().setName(buttonText);
    }

    public static void clickButton(Page page, String buttonText) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonText)).click();
    }

    public static Locator getButtonByText(Page page, String buttonText) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonText));
    }

    public static Locator getRadioButtonByLabel(Page page, String label) {
        return page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(label));
    }

    public static Locator getListBoxByLabel(Page page, String label) {
        return page.getByRole(AriaRole.LISTBOX, new Page.GetByRoleOptions().setName(label));
    }

    public static Locator getCheckBoxByLabel(Page page, String label) {
        return page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName(label));
    }

    public static Locator getTabByText(Page page, String tabName) {
        return page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(tabName));
    }

    public static Locator getTextBoxByLabel(Page page, String label) {
        return page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(label));
    }

    public static String getValueFromTableFor(Page page, String rowHeader) {
        return page.locator("th:has-text(\"" + rowHeader + "\") + td").textContent().trim();
    }

    public static String getInnerTextContentFromTableFor(Page page, String rowHeader) {
        return page.locator("th:has-text(\"" + rowHeader + "\") + td").innerText();
    }

    public static String getCaseStatus(Page page) {
        return page.locator("th:has-text(\"EndState\") + td").textContent();
    }

    public static String getCaseUrl(String caseNumber) {
        return CASE_API_BASE_URL + "/cases/case-details/" + caseNumber.replace("-", "") + "#History";
    }

    public static void clickLink(Page page, String linkText) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(linkText)).click();
    }

    public static String getCaseNumber(Page page) {
        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        return page.locator("ccd-markdown markdown h3")
            .textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
    }
}
