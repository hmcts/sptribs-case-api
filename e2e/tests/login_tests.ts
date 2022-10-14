import loginPage from "../features/home/pages/login";
import config from '../config';

Feature('login');

Scenario('login to sptribs @test', async ({ I }) => {
    // await I.login('user');
    loginPage.open();
    loginPage.login(config.username, config.password);
});
