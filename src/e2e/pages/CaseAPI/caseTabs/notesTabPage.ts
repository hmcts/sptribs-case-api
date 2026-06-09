import { Page } from "@playwright/test";
import commonHelpers from "../../../helpers/commonHelpers.ts";
import notesTab_content from "../../../fixtures/content/CaseAPI/caseTabs/notesTab_content.ts";
import addCaseNotes_content from "../../../fixtures/content/CaseAPI/addNote/addCaseNotes_content.ts";

type NotesTabPage = {
  checkAddedNote(page: Page): Promise<void>;
};

const notesTabPage: NotesTabPage = {
  async checkAddedNote(page: Page): Promise<void> {
    await page.waitForSelector(
      `.case-viewer-label:text-is("${notesTab_content.textOnPage1}")`,
    );
    await Promise.all([
      commonHelpers.checkVisibleAndPresent(
        page.locator(
          `.case-viewer-label:text-is("${notesTab_content.textOnPage1}")`,
        ),
        1,
      ),
      ...Array.from({ length: 4 }, (_, index: number) => {
        const textOnPage = (notesTab_content as any)[`textOnPage${index + 2}`];
        return commonHelpers.checkVisibleAndPresent(
          page.locator(`.text-16:text-is("${textOnPage}")`),
          1,
        );
      }),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("sptribswa hearingcentreadmin")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`.text-16:text-is("${await commonHelpers.todayDate()}")`),
        1,
      ),
      commonHelpers.checkVisibleAndPresent(
        page.locator(`span:text-is("${addCaseNotes_content.textContent}")`),
        1,
      ),
    ]);
  },
};

export default notesTabPage;
