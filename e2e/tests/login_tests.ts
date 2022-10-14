import loginPage from "../features/home/pages/login";
import config from '../config';

Feature('Login');

Scenario('Login to sptribs @test', async ({ I }) => {
    // await I.amOnPage('/');
    // await I.login('user');
    loginPage.open();
    loginPage.acceptCookies();
    loginPage.login(config.username, config.password);
});
