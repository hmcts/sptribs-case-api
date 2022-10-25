import config from '../config';

Feature('Create case');

Scenario.only('Create case @test', async ({ I }) => {
    await I.amOnPage('/');
    await I.login(config.username);
    await I.waitForVisible('h2', 60);
    await I.see('Filters', 'h2');
    await I.click('Create case');
    await I.createCase();
    // await I.createCase();
});