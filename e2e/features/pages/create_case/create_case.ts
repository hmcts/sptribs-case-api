import hasSelector from '../../../helpers/browser_helper';

const I = actor();

const caseFiltersSelectors = {
  jurisdiction: '#cc-jurisdiction',
  caseType: '#cc-case-type',
  event: '#cc-event',
  start_button: 'Start'
};

const caseCategorisationSelectors = {
  caseCategory: '#cicCaseCaseCategory',
  caseSubCategory: '#cicCaseCaseSubcategory',
  continueButton: 'Continue',
  cancelLink: 'Cancel',
};

const caseReceivedDateSelectors = {
  day: '#cicCaseCaseReceivedDate-day',
  month: '#cicCaseCaseReceivedDate-month',
  year: '#cicCaseCaseReceivedDate-year',
}

const identifiedPartiesSelectors = {
    subject: '#cicCasePartiesCIC-SubjectCIC',
    representative: '#cicCasePartiesCIC-RepresentativeCIC',
    applicant: '#cicCasePartiesCIC-ApplicantCIC',
}

const subjectSelectors = {
    subjectFullName: '#cicCaseFullName',
    subjectPhoneNumber: '#cicCasePhoneNumber',
    subjectDobDay: '#cicCaseDateOfBirth-day',
    subjectDobMonth: '#cicCaseDateOfBirth-month',
    subjectDobYear: '#cicCaseDateOfBirth-year',
    subjectContactPreferenceEmail: '#cicCaseContactPreferenceType-Email',
    subjectContactPreferencePost: '#cicCaseContactPreferenceType-Post',
    subjectEmailAddress: '#cicCaseEmail',
}

const addressSelectors = {
    addressPostCodeLookUp: '#cicCaseAddress_cicCaseAddress_postcodeLookup',
    addressList: '#cicCaseAddress_cicCaseAddress_addressList',
    buildingAndStreet: '#cicCaseAddress__detailAddressLine1',
    addressLine2: '#cicCaseAddress__detailAddressLine2',
    addressLine3: '#cicCaseAddress__detailAddressLine3',
    townOrCity: '#cicCaseAddress__detailPostTown',
    countyOrState: '#cicCaseAddress__detailCounty',
    country: '#cicCaseAddress__detailCountry',
    postCode: '#cicCaseAddress__detailPostCode',
}

class CreateCasePage {

  selectCaseFilters() {
    I.selectOption(caseFiltersSelectors.jurisdiction, 'CIC');
    I.selectOption(caseFiltersSelectors.caseType, 'CIC Case Type');
    I.selectOption(caseFiltersSelectors.event, 'Create Case');
    // I.click(caseFiltersSelectors.start_button);
  }

  fillCaseCategorisationForm() {
    I.selectOption(caseCategorisationSelectors.caseCategory, 'Assessment');
    I.selectOption(caseCategorisationSelectors.caseSubCategory, 'Fatal');
  }

  fillCaseReceivedDateForm() {
    I.fillField(caseReceivedDateSelectors.day, 22);
    I.fillField(caseReceivedDateSelectors.month, 3);
    I.fillField(caseReceivedDateSelectors.year, 2011);
  }

  fillIdentifiedPartiesForm() {
    I.checkOption(identifiedPartiesSelectors.subject);
    if(new hasSelector(identifiedPartiesSelectors.representative))
        I.checkOption(identifiedPartiesSelectors.representative);
    if(new hasSelector(identifiedPartiesSelectors.applicant))
        I.checkOption(identifiedPartiesSelectors.applicant);
  }

  fillSubjectDetailsForm(contactPreference: string) {
    I.fillField(subjectSelectors.subjectFullName, 'Subject xyz');
    I.fillField(subjectSelectors.subjectPhoneNumber, '0178253643');
    I.fillField(subjectSelectors.subjectDobDay, '12');
    I.fillField(subjectSelectors.subjectDobMonth, '5');
    I.fillField(subjectSelectors.subjectDobYear, '1990');
    if(contactPreference.toLowerCase() == 'email') {
        I.click(subjectSelectors.subjectContactPreferenceEmail);
        I.waitForVisible(subjectSelectors.subjectEmailAddress, 1);
        I.fillField(subjectSelectors.subjectEmailAddress, 'subject@email.com')
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(subjectSelectors.subjectContactPreferencePost);
        I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
    }
  }

  fillAddressDetails () {

  }
};

export default new CreateCasePage();