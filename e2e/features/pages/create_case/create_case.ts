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

const subjectDetailsSelectors = {
    subjectFullName: '#cicCaseFullName',
    subjectPhoneNumber: '#cicCasePhoneNumber',
    subjectDobDay: '#cicCaseDateOfBirth-day',
    subjectDobMonth: '#cicCaseDateOfBirth-month',
    subjectDobYear: '#cicCaseDateOfBirth-year',
    subjectContactPreferenceEmail: '#cicCaseContactPreferenceType-Email',
    subjectContactPreferencePost: '#cicCaseContactPreferenceType-Post',
    subjectEmailAddress: '#cicCaseEmail',
}

const applicantDetailsSelectors = {
  applicantFullName: '#cicCaseApplicantFullName',
  applicantPhoneNumber: '#cicCaseApplicantPhoneNumber',
  applicantEmailAddress: '#cicCaseApplicantEmailAddress',
  applicantDobDay: '#cicCaseApplicantDateOfBirth-day',
  applicantDobMonth: '#cicCaseApplicantDateOfBirth-month',
  applicantDobYear: '#cicCaseApplicantDateOfBirth-year',
  applicantContactPreferenceEmail: '#cicCaseApplicantContactDetailsPreference-Email',
  applicantContactPreferencePost: '#cicCaseApplicantContactDetailsPreference-Post'
}

const representativeDetailsSelectors = {
  representativeFullName: '#cicCaseRepresentativeFullName',
  representativeOrganisation: '#cicCaseRepresentativeOrgName',
  representativeContactNumber: '#cicCaseRepresentativePhoneNumber',
  representativeEmailAddress: '#cicCaseRepresentativeEmailAddress',
  representativeReference: '#cicCaseRepresentativeReference',
  representativeLegallyQualifiedYes: '#cicCaseIsRepresentativeQualified_Yes',
  representativeLegallyQualifiedNo: '#cicCaseIsRepresentativeQualified_No',
  representativeContactPreferenceEmail: '#cicCaseRepresentativeContactDetailsPreference-Email',
  representativeContactPreferencePost: '#cicCaseRepresentativeContactDetailsPreference-Post'
}

const addressSelectors = {
    addressPostCodeLookUp: '#cicCaseAddress_cicCaseAddress_postcodeInput',
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
    I.fillField(subjectDetailsSelectors.subjectFullName, 'Subject xyz');
    I.fillField(subjectDetailsSelectors.subjectPhoneNumber, '0178253643');
    I.fillField(subjectDetailsSelectors.subjectDobDay, '12');
    I.fillField(subjectDetailsSelectors.subjectDobMonth, '5');
    I.fillField(subjectDetailsSelectors.subjectDobYear, '1990');
    if(contactPreference.toLowerCase() == 'email') {
        I.click(subjectDetailsSelectors.subjectContactPreferenceEmail);
        I.waitForVisible(subjectDetailsSelectors.subjectEmailAddress, 1);
        I.fillField(subjectDetailsSelectors.subjectEmailAddress, 'subject@email.com');
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(subjectDetailsSelectors.subjectContactPreferencePost);
        I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
        this.fillAddressDetails();
    }
  }

  fillApplicantDetailsForm(contactPreference: string) {
    I.fillField(applicantDetailsSelectors.applicantFullName, 'Subject xyz');
    I.fillField(applicantDetailsSelectors.applicantPhoneNumber, '0178253643');
    I.fillField(applicantDetailsSelectors.applicantEmailAddress, 'applicant@email.com');
    I.fillField(applicantDetailsSelectors.applicantDobDay, '5');
    I.fillField(applicantDetailsSelectors.applicantDobMonth, '12');
    I.fillField(applicantDetailsSelectors.applicantDobYear, '1995');
    if(contactPreference.toLowerCase() == 'email') {
        I.click(applicantDetailsSelectors.applicantContactPreferenceEmail);
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(applicantDetailsSelectors.applicantContactPreferencePost);
        // I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
        this.fillAddressDetails();
    }
  }

  fillRepresentativeDetailsForm(contactPreference: string) {
    I.fillField(representativeDetailsSelectors.representativeFullName, 'Representative qwerty');
    I.fillField(representativeDetailsSelectors.representativeOrganisation, 'Test Solicitors');
    I.fillField(representativeDetailsSelectors.representativeContactNumber, '0178253643');
    I.fillField(representativeDetailsSelectors.representativeReference, 'representative@email.com');
    I.click(representativeDetailsSelectors.representativeLegallyQualifiedYes);
    if(contactPreference.toLowerCase() == 'email') {
        I.click(representativeDetailsSelectors.representativeContactPreferenceEmail);
        I.waitForVisible(representativeDetailsSelectors.representativeEmailAddress, 1);
        I.fillField(representativeDetailsSelectors.representativeEmailAddress, 'representative@email.com');
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(representativeDetailsSelectors.representativeContactPreferencePost);
        // I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
        this.fillAddressDetails();
    }
  }

  fillAddressDetails () {
    // I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
    // I.fillField(addressSelectors.addressPostCodeLookUp, 'S');
    // I.click('Find address');
    // I.waitForVisible(addressSelectors.addressList, 3);
    // I.selectOption(addressSelectors.addressList, '1 Rse Way, London');
    // I.fillField(addressSelectors.addressLine2, 'Address Line 2');
    // I.fillField(addressSelectors.addressLine3, 'Address Line 3');
    I.fillField('Enter a UK postcode', 'S');
    I.click('Find address');
    I.selectOption('Select an address', '1 Rse Way, London');
    I.fillField('Address Line 2', 'Address Line 2');
    I.fillField('Address Line 3', 'Address Line 3');
  }
};

export default new CreateCasePage();