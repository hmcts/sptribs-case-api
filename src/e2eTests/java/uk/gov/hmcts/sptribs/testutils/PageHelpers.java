package uk.gov.hmcts.sptribs.testutils;

import com.microsoft.playwright.Page;

public class PageHelpers {

    private PageHelpers() {
    }

    public static Page.WaitForSelectorOptions selectorOptionsWithTimeout(int timeout) {
        Page.WaitForSelectorOptions options = new Page.WaitForSelectorOptions();
        options.setTimeout(timeout);
        return options;
    }

    public static Page.WaitForURLOptions urlOptionsWithTimeout(int timeout) {
        Page.WaitForURLOptions options = new Page.WaitForURLOptions();
        options.setTimeout(timeout);
        return options;
    }

    public static Page.WaitForFunctionOptions functionOptionsWithTimeout(int timeout) {
        Page.WaitForFunctionOptions options = new Page.WaitForFunctionOptions();
        options.setTimeout(timeout);
        return options;
    }
}
