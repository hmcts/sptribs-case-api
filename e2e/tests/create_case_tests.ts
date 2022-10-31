Feature('Create case');

Scenario('Create case @test', async ({ I }) => {
    await I.amOnPage('/');
    let caseNumber = await I.createCase();
    let caseNumberDehyphenated = caseNumber.replace(/-/g, '');
    await I.amOnPage(`/cases/case-details/${caseNumberDehyphenated}#History`);
    await I.see(caseNumber, 'h3');
    await I.see('History', 'h2'); // I wait for History header to appear
});
