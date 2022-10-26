import loginPage from './features/pages/login/login';
import createCasePage from './features/pages/create_case/create_case';
import config from './config';
// import output from 'codeceptjs';

const selectors = {
  signed_in_selector: 'exui-header',
  signed_out_selector: '#global-header',
  sign_in_page_title: 'h1'
}


// declare namespace CodeceptJS
export = () => {
    return actor({
        login: async function(user) {
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
          createCasePage.selectCaseFilters();
          this.click('Start');
          createCasePage.fillCaseCategorisationForm();
          this.click('Continue');
          createCasePage.fillCaseReceivedDateForm();
          this.click('Continue');
          createCasePage.fillIdentifiedPartiesForm();
          this.click('Continue');
          createCasePage.fillSubjectDetailsForm('post');
          this.click('Continue');
          createCasePage.fillApplicantDetailsForm('post');
          this.click('Continue');
          createCasePage.fillRepresentativeDetailsForm('post');
          this.click('Continue');
        },

        async signOut() {
            await this.retryUntilExists(() => {
              this.click('Sign out');
            }, selectors.signed_out_selector);
          },

        async retryUntilExists(action, locator, maxNumberOfTries = 6) {
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