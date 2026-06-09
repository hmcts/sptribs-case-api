# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: createSummary.test.ts >> Create hearing summary tests >> Create hearing summary - hearing outcome is allowed.
- Location: src/e2e/scripts/createSummary.test.ts:13:8

# Error details

```
Error: page.waitForSelector: Target page, context or browser has been closed
Call log:
  - waiting for locator('.heading-h1:text-is("Create Case")') to be visible

```

# Test source

```ts
  1  | import { AxeUtils } from "@hmcts/playwright-common";
  2  | import { Page } from "@playwright/test";
  3  | import confirm_content from "../../../fixtures/content/CaseAPI/createCase/confirm_content.ts";
  4  | import commonHelpers from "../../../helpers/commonHelpers.ts";
  5  | 
  6  | type ConfirmPage = {
  7  |   closeAndReturn: string;
  8  |   checkPageLoads(page: Page, accessibilityTest: boolean): Promise<void>;
  9  |   returnCaseNumber(page: Page): Promise<string>;
  10 |   closeAndReturnToCase(page: Page): Promise<void>;
  11 | };
  12 | 
  13 | const createCaseConfirmPage: ConfirmPage = {
  14 |   closeAndReturn: ".button",
  15 | 
  16 |   async checkPageLoads(page, accessibilityTest): Promise<void> {
> 17 |     await page.waitForSelector(
     |                ^ Error: page.waitForSelector: Target page, context or browser has been closed
  18 |       `.heading-h1:text-is("${confirm_content.pageTitle}")`,
  19 |     );
  20 |     await Promise.all([
  21 |       commonHelpers.checkVisibleAndPresent(
  22 |         page.locator(`markdown > h1:text-is("${confirm_content.subTitle1}")`),
  23 |         1,
  24 |       ),
  25 |       commonHelpers.checkVisibleAndPresent(
  26 |         page.locator(`markdown > h2:text-is("${confirm_content.textOnPage1}")`),
  27 |         1,
  28 |       ),
  29 |     ]);
  30 |     const caseElement = await page.$$("markdown > h2");
  31 | 
  32 |     const caseElementLength16 = await Promise.all(
  33 |       caseElement.map(async (element) => {
  34 |         const text = await page.evaluate(
  35 |           (element) => element.textContent,
  36 |           element,
  37 |         );
  38 |         if (text && text.trim().length === 16) {
  39 |           // Check if text is not null
  40 |           return element;
  41 |         }
  42 |       }),
  43 |     );
  44 | 
  45 |     const filteredCaseElement = caseElementLength16.filter(
  46 |       (element) => element !== null,
  47 |     );
  48 | 
  49 |     if (!(filteredCaseElement.length > 0)) {
  50 |       console.log("Invalid case reference.");
  51 |       process.exit(1);
  52 |     }
  53 |     if (accessibilityTest) {
  54 |       await new AxeUtils(page).audit();
  55 |     }
  56 |   },
  57 | 
  58 |   async returnCaseNumber(page: Page): Promise<string> {
  59 |     try {
  60 |       let cicCaseData: string =
  61 |         (await page.textContent("h2:nth-child(3)")) ?? "Empty";
  62 |       cicCaseData = cicCaseData.replace(/\D/g, "");
  63 |       cicCaseData = cicCaseData.replace(/(\d{4})/g, "$1-");
  64 |       cicCaseData = cicCaseData.slice(0, -1);
  65 |       return cicCaseData;
  66 |     } catch (error) {
  67 |       console.error(
  68 |         "Error occurred with capturing the case number reference.",
  69 |         error,
  70 |       );
  71 |       throw error;
  72 |     }
  73 |   },
  74 | 
  75 |   async closeAndReturnToCase(page: Page): Promise<void> {
  76 |     await page.locator(this.closeAndReturn).click();
  77 |     await page.waitForSelector(`h2:text-is("History")`);
  78 |     await page.waitForSelector(`.mat-tab-label-content:text-is("Tasks")`);
  79 |   },
  80 | };
  81 | 
  82 | export default createCaseConfirmPage;
  83 | 
```