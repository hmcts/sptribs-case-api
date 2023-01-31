package uk.gov.hmcts.sptribs.e2e;

import com.microsoft.playwright.options.SelectOption;
import uk.gov.hmcts.sptribs.testutils.DateHelpers;
import uk.gov.hmcts.sptribs.testutils.PageHelpers;
import uk.gov.hmcts.sptribs.testutils.StringHelpers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class Case extends Base {

    public String createCase(String... args) {

        // Select case filters
        PageHelpers.clickLink("Create case");
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Create Case'",
            "h1", PageHelpers.functionOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).options.length > 1",
            "#cc-jurisdiction", PageHelpers.functionOptionsWithTimeout(60000));
        page.selectOption("#cc-jurisdiction", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-case-type", new SelectOption().setLabel("CIC"));
        page.selectOption("#cc-event", new SelectOption().setLabel("Create Case"));
        page.locator("ccd-create-case-filters button[type='submit']").click();
        PageHelpers.clickButton("Start");

        // Select case categories
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case categorisation'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        page.selectOption("#cicCaseCaseCategory", new SelectOption().setLabel("Assessment")); // Available options: Assessment, Eligibility
        page.selectOption("#cicCaseCaseSubcategory", new SelectOption().setLabel("Fatal")); // Available options: Fatal, Sexual Abuse, Other
        PageHelpers.clickButton("Continue");

        // Fill date case received form
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'When was the case received?'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        Calendar date = DateHelpers.getYesterdaysDate();
        PageHelpers.getTextBoxByLabel("Day").fill(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        PageHelpers.getTextBoxByLabel("Month").fill(String.valueOf(date.get(Calendar.MONTH) + 1));
        PageHelpers.getTextBoxByLabel("Year").type(String.valueOf(date.get(Calendar.YEAR)));
        PageHelpers.clickButton("Continue");

        // Fill identified parties form
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Who are the parties in this case?'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.getCheckBoxByLabel("Subject").first().check();
        List<String> options = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toList());
        if (options.contains("representative")) {
            PageHelpers.getCheckBoxByLabel("Representative").check();
        }
        if (options.contains("applicant")) {
            PageHelpers.getCheckBoxByLabel("Applicant (if different from subject)").check();
        }
        PageHelpers.clickButton("Continue");

        // Fill subject details form
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Who is the subject of this case?'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.getTextBoxByLabel("Subject\'s full name").fill("Subject " + StringHelpers.getRandomString(9));
        PageHelpers.getTextBoxByLabel("Subject\'s phone number (Optional)").fill("07465730467");
        PageHelpers.getTextBoxByLabel("Day").fill("1");
        PageHelpers.getTextBoxByLabel("Month").fill("1");
        PageHelpers.getTextBoxByLabel("Year").fill("1990");
        page.locator("text = What is subject\'s contact preference type?").click();
        if (options.contains("post")) {
            PageHelpers.getRadioButtonByLabel("Post").click();
            fillAddressDetails("subject");
        } else {
            PageHelpers.getRadioButtonByLabel("Email").click();
            PageHelpers.getTextBoxByLabel("Subject\'s email address")
                        .fill(StringHelpers.getRandomString(9).toLowerCase() + "@sub.com");
        }
        PageHelpers.clickButton("Continue");

        // Fill contact preferences form
        PageHelpers.getCheckBoxByLabel("Subject").first().check();
        if (page.isVisible("#cicCaseApplicantCIC-ApplicantCIC")) {
            PageHelpers.getCheckBoxByLabel("Applicant (if different from subject)").check();
        }
        if (page.isVisible("#cicCaseRepresentativeCIC-RepresentativeCIC")) {
            PageHelpers.getCheckBoxByLabel("Representative").check();
        }
        PageHelpers.clickButton("Continue");

        // Upload tribunals form
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Upload tribunal forms'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.clickButton("Continue");

        // Fill further details form
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Enter further details about this case'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        page.selectOption("#cicCaseSchemeCic", new SelectOption().setLabel("2001"));
        page.locator("#cicCaseClaimLinkedToCic_Yes").click();
        PageHelpers.getTextBoxByLabel("CICA reference number").fill("CICATestReference");
        page.locator("#cicCaseCompensationClaimLinkCIC_Yes").click();
        PageHelpers.getTextBoxByLabel("Police Authority Management Incident").fill("PAMITestReference");
        page.locator("#cicCaseFormReceivedInTime_Yes").click();
        page.locator("#cicCaseMissedTheDeadLineCic_Yes").click();
        PageHelpers.clickButton("Continue");

        // Check your answers form
        page.waitForSelector("h2.heading-h2", PageHelpers.selectorOptionsWithTimeout(20000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Check your answers'",
            "h2.heading-h2", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.clickButton("Save and continue");

        // Case created confirmation screen
        page.waitForSelector("ccd-markdown markdown h1", PageHelpers.selectorOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case Created'",
            "ccd-markdown markdown h1", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.clickButton("Close and Return to case details");

        // Case details screen
        page.waitForSelector("h2.heading-h2", PageHelpers.selectorOptionsWithTimeout(20000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'History'",
            "h2.heading-h2", PageHelpers.functionOptionsWithTimeout(20000));
        String caseNumberHeading = page.locator("ccd-markdown markdown h3").textContent();
        String caseNumber = page.locator("ccd-markdown markdown h3").textContent().substring(caseNumberHeading.lastIndexOf(" ")).trim();
        return caseNumber;
    }

    public void buildCase() {
        page.selectOption("#next-step", new SelectOption().setLabel("Case: Build case"));
        page.waitForFunction("selector => document.querySelector(selector).disabled === false",
            "ccd-event-trigger button[type='submit']", PageHelpers.functionOptionsWithTimeout(3000));
        PageHelpers.clickButton("Go");
        page.waitForSelector("h1", PageHelpers.selectorOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case: Build case'",
            "h1", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.clickButton("Save and continue");
        page.waitForSelector("ccd-markdown markdown h1", PageHelpers.selectorOptionsWithTimeout(60000));
        page.waitForFunction("selector => document.querySelector(selector).innerText === 'Case built successful'",
            "ccd-markdown markdown h1", PageHelpers.functionOptionsWithTimeout(20000));
        PageHelpers.clickButton("Close and Return to case details");
    }

    private void fillAddressDetails(String partyType) {
        PageHelpers.getTextBoxByLabel("Enter a UK postcode").fill("SW11 1PD");
        PageHelpers.clickLink("I can\'t enter a UK postcode");
        switch (partyType.toLowerCase()) {
            case "subject":
                PageHelpers.getTextBoxByLabel("Building and Street").fill("1 Subject Rse Way");
                break;
            case "applicant":
                PageHelpers.getTextBoxByLabel("Building and Street").fill("1 Applicant Rse Way");
                break;
            case "representative":
                PageHelpers.getTextBoxByLabel("Building and Street").fill("1 Representative Rse Way");
                break;
            default:
                System.out.println("Invalid part type");
        }
        PageHelpers.getTextBoxByLabel("Address Line 2").fill("Test Address Line 2");
        PageHelpers.getTextBoxByLabel("Address Line 3").fill("Test Address Line 3");
        PageHelpers.getTextBoxByLabel("Town or City").fill("London");
        PageHelpers.getTextBoxByLabel("County/State").fill("London");
        PageHelpers.getTextBoxByLabel("Country").fill("UK");
        PageHelpers.getTextBoxByLabel("Postcode/Zipcode").fill("SW11 1PD");
    }
}
