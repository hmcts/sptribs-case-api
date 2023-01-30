package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.options.SelectOption;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class CreateCase extends Base {

    public String createACase(String... args) {

        // Select case filters
        page.locator("text = Create case").click();
        page.waitForFunction("() => document.querySelector('h1').innerText === 'Create Case'",
            "h1", PageHelpers.functionOptionsWithTimeout(60000));
        page.waitForFunction("() => document.querySelector('#cc-jurisdiction').options.length > 1",
            "#cc-jurisdiction", PageHelpers.functionOptionsWithTimeout(60000));
        page.selectOption("#cc-jurisdiction", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-case-type", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-event", new SelectOption().setLabel("Create Case"));
        page.locator("ccd-create-case-filters button[type='submit']").click();

        // Select case categories
        page.waitForFunction("() => document.querySelector('h1').innerText === 'Case categorisation'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        page.selectOption("#cicCaseCaseCategory", new SelectOption().setLabel("Assessment")); // Available options: Assessment, Eligibility
        page.selectOption("#cicCaseCaseSubcategory", new SelectOption().setLabel("Fatal")); // Available options: Fatal, Sexual Abuse, Other
        page.locator("text = Continue").click();

        // Fill date case received form
        page.waitForFunction("() => document.querySelector('h1').innerText === 'When was the case received?'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        Calendar date = DateHelpers.getYesterdaysDate();
        page.locator("#cicCaseCaseReceivedDate-day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        page.locator("#cicCaseCaseReceivedDate-month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        page.locator("#cicCaseCaseReceivedDate-year").type(String.valueOf(date.get(Calendar.YEAR)));
        page.locator("text = Continue").click();

        // Fill identified parties form
        page.waitForFunction("() => document.querySelector('h1').innerText === 'Who are the parties in this case?'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        page.locator("#cicCasePartiesCIC-SubjectCIC").check();
        List<String> options = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toList());
        if (options.contains("representative")) {
            page.locator("#cicCasePartiesCIC-RepresentativeCIC").check();
        }
        if (options.contains("applicant")) {
            page.locator("#cicCasePartiesCIC-ApplicantCIC").check();
        }
        page.locator("text = Continue").click();

        // Fill subject details form
        page.waitForFunction("() => document.querySelector('h1').innerText === 'Who is the subject of this case?'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        page.locator("#cicCaseFullName").fill("Subject " + StringHelpers.getRandomString(9));
        page.locator("#cicCasePhoneNumber").fill("07465730467");
        page.locator("#cicCaseDateOfBirth-day").fill("1");
        page.locator("#cicCaseDateOfBirth-month").fill("1");
        page.locator("#cicCaseDateOfBirth-year").fill("1990");
        if (options.contains("post")) {
            page.locator("#cicCaseContactPreferenceType-Post").click();
            fillAddressDetails("subject");
        } else {
            page.locator("text = What is subject\'s contact preference type?").click();
            page.waitForSelector("#cicCaseContactPreferenceType-Email", PageHelpers.selectorOptionsWithTimeout(3000));
            page.locator("input[type='radio']#cicCaseContactPreferenceType-Email").click();
            page.waitForSelector("#cicCaseEmail", PageHelpers.selectorOptionsWithTimeout(3000));
            page.locator("#cicCaseEmail").fill(StringHelpers.getRandomString(9).toLowerCase() + "@sub.com");
        }
        page.locator("text = Continue").click();

        // Fill contact preferences form
        page.locator("#cicCaseSubjectCIC-SubjectCIC").check();

        return "";
    }

    private void fillAddressDetails(String partyType) {
        page.locator(" text = Enter a UK postcode").fill("SW11 1PD");
        page.locator("text = I can\'t enter a UK postcode").click();
        switch (partyType.toLowerCase()) {
            case "subject":
                page.locator("text = Building and Street").fill("1 Subjct Rse Way");
                break;
            case "applicant":
                page.locator("text = Building and Street").fill("1 Applcnt Rse Way");
                break;
            case "representative":
                page.locator("text = Building and Street").fill("1 Reprsntatve Rse Way");
                break;
            default:
                System.out.println("Invalid part type");
        }
        page.locator("text = Address Line 2").fill("Test Address Line 2");
        page.locator("text = Address Line 3").fill("Test Address Line 3");
        page.locator("text = Town or City").fill("London");
        page.locator("text = County/State").fill("London");
        page.locator("text = Country").fill("UK");
        page.locator("text = Postcode/Zipcode").fill("SW11 1PD");
    }
}
