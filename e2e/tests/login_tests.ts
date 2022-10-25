import config from '../config';

Feature('Login');

Scenario('Login to sptribs @test1', async ({ I }) => {
    await I.amOnPage('/');
    await I.login(config.username);
});
