const I = actor();

const fields = {
  username: '#username',
  password: '#password',
};
const buttons = {
  submit: 'input.button',
  hmctsSignIn: 'input[type="submit"][name="save"]',
  acceptCookies: 'button[value="accept"]',
  hideMessage: 'button[name="hide-accepted"]',
};

class LoginPage {
  open () {
    I.amOnPage('/');
  }

  acceptCookies () {
    I.click(buttons.acceptCookies);
    I.waitForInvisible(buttons.acceptCookies);
    I.click(buttons.hideMessage);
    I.waitForInvisible(buttons.hideMessage);
  }

  login (email: string, password: string) {
    I.waitForVisible(fields.username, 60);
    I.fillField(fields.username, email);
    I.fillField(fields.password, password);
    I.click(buttons.submit);
    I.waitInUrl('/cases', 60)
    // I.seeInCurrentUrl('/cases');
  }
  
};

export default new LoginPage();