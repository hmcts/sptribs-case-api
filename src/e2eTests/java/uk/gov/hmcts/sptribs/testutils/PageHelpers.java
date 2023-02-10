package uk.gov.hmcts.sptribs.testutils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import uk.gov.hmcts.sptribs.e2e.Base;

public class PageHelpers extends Base {

    public static Page.WaitForSelectorOptions selectorOptionsWithTimeout(int timeout) {
        return new Page.WaitForSelectorOptions().setTimeout(timeout);
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

    public static void clickButton(String buttonText) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonText)).click();
    }

    public static Locator getButtonByText(String buttonText) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonText));
    }

    public static Locator getRadioButtonByLabel(String label) {
        return page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(label));
    }

    public static Locator getListBoxByLabel(String label) {
        return page.getByRole(AriaRole.LISTBOX, new Page.GetByRoleOptions().setName(label));
    }

    public static Locator getCheckBoxByLabel(String label) {
        return page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName(label));
    }

    public static Locator getTextBoxByLabel(String label) {
        return page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(label));
    }

    public static void clickLink(String linkText) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(linkText)).click();
    }
}
