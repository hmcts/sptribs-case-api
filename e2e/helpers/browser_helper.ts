class BrowserHelpers extends Helper {
  static hasSelector(acceptCookies: string) {
    throw new Error('Method not implemented.');
  }

  getHelper() {
    return this.helpers['Playwright'] || this.helpers['WebDriver'];
  }

  isPlaywright(){
    return this.helpers['Playwright'];
  }

  /**
   * Finds elements described by selector.
   * If element cannot be found an empty collection is returned.
   *
   * @param selector - element selector
   * @returns {Promise<Array>} - promise holding either collection of elements or empty collection if element is not found
   */
  async locateSelector(selector: any): Promise<Array<any>> {
    return this.getHelper()._locate(selector);
  }

  async hasSelector(selector: any) {
    return (await this.locateSelector(selector)).length;
  }

  /**
   * Finds element described by locator.
   * If element cannot be found immediately function waits specified amount of time or globally configured `waitForTimeout` period.
   * If element still cannot be found after the waiting time an undefined is returned.
   *
   * @param locator - element CSS locator
   * @param sec - optional time in seconds to wait
   * @returns {Promise<undefined|*>} - promise holding either an element or undefined if element is not found
   */
  async waitForSelector(locator: any, sec: number): Promise<undefined | any> {
    const helper = this.getHelper();
    const waitTimeout = sec ? sec * 1000 : helper.options.waitForTimeout;
    try {
      if (this.isPlaywright()) {
        const context = await helper._getContext();
        return await context.waitForSelector(locator, {timeout: waitTimeout});
      } else {
        return await helper.waitForElement(locator, waitTimeout);
      }
    } catch (error) {
      return undefined;
    }
  }
};

export = BrowserHelpers;
