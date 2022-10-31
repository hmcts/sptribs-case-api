import loginPage from './pages/login';
import createCasePage from './pages/create_case';
import config from './config';
// import output from 'codeceptjs';

const selectors = {
  signed_in_selector: 'exui-header',
  signed_out_selector: '#global-header',
  sign_in_page_title: 'h1'
}

const buttons = {
  continue_button: 'Continue',
  save_and_continue_button: 'Save and continue',
  start_button: 'Start'
}


// declare namespace CodeceptJS
export = () => {
    return actor({
        async login(user) {
            const signOutElement = await this.grabTextFrom(selectors.sign_in_page_title);
            if (await this.hasSelector(selectors.signed_in_selector)) {
                await this.signOut();
            }

            await this.retryUntilExists(async () => {
                await this.amOnPage(config.url.local, 90);
                console.log(`Signing in as user: ${user}`);
                loginPage.login(user, config.password);
            }, selectors.sign_in_page_title);
        },
        async createCase() {
          await this.login(config.username);
          await this.waitForVisible('h2', 60);
          await this.see('Filters', 'h2');
          await this.click('Create case');
          createCasePage.selectCaseFilters();
          await this.click(buttons.start_button);
          createCasePage.fillCaseCategorisationForm();
          await this.click(buttons.continue_button);
          createCasePage.fillCaseReceivedDateForm();
          await this.click(buttons.continue_button);
          createCasePage.fillIdentifiedPartiesForm();
          await this.click(buttons.continue_button);
          createCasePage.fillSubjectDetailsForm('post');
          await this.click(buttons.continue_button);
          createCasePage.fillApplicantDetailsForm('post');
          await this.click(buttons.continue_button);
          createCasePage.fillRepresentativeDetailsForm('post');
          await this.click(buttons.continue_button);
          createCasePage.fillContactPreferencesForm();
          await this.click(buttons.continue_button);
          createCasePage.uploadFiles();
          await this.click(buttons.continue_button);
          createCasePage.fillFurtherDetailsForm();
          await this.click(buttons.continue_button);
          await this.see('Check your answers', 'h2');
          await this.click(buttons.save_and_continue_button);
          await this.see('Case Created', 'h1');
          let caseNumber = await this.grabTextFrom('//h2[2]');
          console.log(caseNumber);
          return caseNumber;
        },

        async signOut() {
            await this.retryUntilExists(() => {
              this.click('Sign out');
            }, selectors.signed_out_selector);
          },

        async retryUntilExists(action, locator, maxNumberOfTries = 3) {
            for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
              console.log(`retryUntilExists(${locator}): starting try #${tryNumber}`);
              if (tryNumber > 1 && await this.hasSelector(locator)) {
                console.log(`retryUntilExists(${locator}): element found before try #${tryNumber} was executed`);
                break;
              }
              await action();
              if (await this.waitForSelector(locator) != null) {
                console.log(`retryUntilExists(${locator}): element found after try #${tryNumber} was executed`);
                break;
              } else {
                console.log(`retryUntilExists(${locator}): element not found after try #${tryNumber} was executed`);
              }
              if (tryNumber === maxNumberOfTries) {
                throw new Error(`Maximum number of tries (${maxNumberOfTries}) has been reached in search for ${locator}`);
              }
            }
          },
    });
}