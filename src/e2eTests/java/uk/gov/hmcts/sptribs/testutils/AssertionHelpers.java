package uk.gov.hmcts.sptribs.testutils;

import com.microsoft.playwright.assertions.LocatorAssertions;
import uk.gov.hmcts.sptribs.e2e.Base;

public class AssertionHelpers extends Base {

    public static LocatorAssertions.HasTextOptions textOptionsWithTimeout(int timeout) {
        return new LocatorAssertions.HasTextOptions().setTimeout(timeout);
    }

    public static LocatorAssertions.HasValueOptions valueOptionsWithTimeout(int timeout) {
        return new LocatorAssertions.HasValueOptions().setTimeout(timeout);
    }

    public static LocatorAssertions.IsVisibleOptions visibleOptionsWithTimeout(int timeout) {
        return new LocatorAssertions.IsVisibleOptions().setTimeout(timeout);
    }

    public static LocatorAssertions.IsEnabledOptions enabledOptionsWithTimeout(int timeout) {
        return new LocatorAssertions.IsEnabledOptions().setTimeout(timeout);
    }
}
