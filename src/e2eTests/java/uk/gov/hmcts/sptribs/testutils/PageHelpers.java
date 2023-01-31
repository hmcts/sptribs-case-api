package uk.gov.hmcts.sptribs.testutils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import uk.gov.hmcts.sptribs.e2e.Base;

public class PageHelpers extends Base {

    private PageHelpers() {
    }

    public static Page.WaitForSelectorOptions selectorOptionsWithTimeout(int timeout) {
        return new Page.WaitForSelectorOptions().setTimeout(timeout);
    }

    public static Page.WaitForURLOptions urlOptionsWithTimeout(int timeout) {
        return new Page.WaitForURLOptions().setTimeout(timeout);
    }

    public static Page.WaitForFunctionOptions functionOptionsWithTimeout(int timeout) {
        return new Page.WaitForFunctionOptions().setTimeout(timeout);
    }

    public static void clickButton(String buttonName) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonName)).click();
    }

    public static Locator getRadioButtonByLabel(String radioButtonName) {
        return page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(radioButtonName));
    }

    public static Locator getListBoxByName(String name) {
        return page.getByRole(AriaRole.LISTBOX, new Page.GetByRoleOptions().setName(name));
    }

    public static Locator getCheckBoxByLabel(String checkBox) {
        return page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName(checkBox));
    }

    public static Locator getTextBoxByLabel(String textBoxName) {
        return page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(textBoxName));
    }

    public static void clickLink(String linkText) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(linkText)).click();
    }
}
