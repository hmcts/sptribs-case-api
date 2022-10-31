import config from '../config';
import BrowserHelpers from '../helpers/browser_helper';
import { caseData } from '../resources/case_data';
import { pageHeadings } from '../resources/page_headings';
const browserHelpers = new BrowserHelpers(config);

const I = actor();

const headingsSelectors = {
  h1_heading: 'h1',
  h2_heading: 'h2',
  h3_heading: 'h3',
  ccd_subheading: 'ccd-markdown p'
}

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

const contactPreferencesSelectors = {
  subject: '#cicCaseSubjectCIC-SubjectCIC',
  applicant: '#cicCaseApplicantCIC-ApplicantCIC',
  representative: '#cicCaseRepresentativeCIC-RepresentativeCIC'
}

const uploadFilesSelectors = {
  addNewButton: '',
  chooseFileButton: "input[type='file']",
  descriptionMultiLineBox: 'div.form-group textarea',
  disabledCancelUploadButton: "button[aria-label='Cancel upload'][disabled]"
}

const furtherDetailsSelectors = {
  scheme: '#cicCaseSchemeCic',
  claimsLodgedWithCicaYes: '#cicCaseClaimLinkedToCic_Yes',
  claimsLodgedWithCicaNo: '#cicCaseClaimLinkedToCic_No',
  cicaReferenceNumber: "#cicCaseCicaReferenceNumber",
  compensationClaimsLinkedYes: '#cicCaseCompensationClaimLinkCIC_Yes',
  compensationClaimsLinkedNo: '#cicCaseCompensationClaimLinkCIC_No',
  policaAuthorityManagementIncident: '#cicCasePoliceAuthority',
  tribunalFormsReceivedYes: '#cicCaseFormReceivedInTime_Yes',
  tribunalFormsReceivedNo: '#cicCaseFormReceivedInTime_No',
  missedDeadlineExplainedYes: '#cicCaseMissedTheDeadLineCic_Yes',
  missedDeadlineExplainedNo: '#cicCaseMissedTheDeadLineCic_No'
}

class CreateCasePage {
  chooseFileButtons: any;
  inputDescriptionTextAreaBoxes: any;
  cancelUploadButtons: any;

  selectCaseFilters() {
    I.selectOption(caseFiltersSelectors.jurisdiction, caseData.case_filters.jurisdiction);
    I.selectOption(caseFiltersSelectors.caseType, caseData.case_filters.case_type);
    I.selectOption(caseFiltersSelectors.event, caseData.case_filters.event);
  }

  fillCaseCategorisationForm() {
    I.see(pageHeadings.case_categorisation_form.subheading, headingsSelectors.ccd_subheading);
    I.selectOption(caseCategorisationSelectors.caseCategory, caseData.case_categorisation.category);
    I.selectOption(caseCategorisationSelectors.caseSubCategory, caseData.case_categorisation.subcategory);
  }

  fillCaseReceivedDateForm() {
    I.see(pageHeadings.date_case_received_form.subheading, headingsSelectors.ccd_subheading);
    I.fillField(caseReceivedDateSelectors.day, caseData.date_case_received.day);
    I.fillField(caseReceivedDateSelectors.month, caseData.date_case_received.month);
    I.fillField(caseReceivedDateSelectors.year, caseData.date_case_received.year);
  }

  fillIdentifiedPartiesForm() {
    I.see(pageHeadings.case_identified_parties_form.subheading, headingsSelectors.ccd_subheading);
    I.checkOption(identifiedPartiesSelectors.subject);
    if(browserHelpers.hasSelector(identifiedPartiesSelectors.representative))
        I.checkOption(identifiedPartiesSelectors.representative);
    if(browserHelpers.hasSelector(identifiedPartiesSelectors.applicant))
        I.checkOption(identifiedPartiesSelectors.applicant);
  }

  fillSubjectDetailsForm(contactPreference: string) {
    I.see(pageHeadings.subject_details_form.subheading, headingsSelectors.h3_heading);
    I.fillField(subjectDetailsSelectors.subjectFullName, caseData.subject_details.full_name);
    I.fillField(subjectDetailsSelectors.subjectPhoneNumber, caseData.subject_details.phone_number);
    I.fillField(subjectDetailsSelectors.subjectDobDay, caseData.subject_details.dob_day);
    I.fillField(subjectDetailsSelectors.subjectDobMonth, caseData.subject_details.dob_month);
    I.fillField(subjectDetailsSelectors.subjectDobYear, caseData.subject_details.dob_year);
    if(contactPreference.toLowerCase() == 'email') {
        I.click(subjectDetailsSelectors.subjectContactPreferenceEmail);
        I.waitForVisible(subjectDetailsSelectors.subjectEmailAddress, 1);
        I.fillField(subjectDetailsSelectors.subjectEmailAddress, caseData.subject_details.email);
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(subjectDetailsSelectors.subjectContactPreferencePost);
        I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
        this.fillAddressDetails();
    }
  }

  fillApplicantDetailsForm(contactPreference: string) {
    I.see(pageHeadings.applicant_details_form.subheading, headingsSelectors.h3_heading)
    I.fillField(applicantDetailsSelectors.applicantFullName, caseData.applicant_details.full_name);
    I.fillField(applicantDetailsSelectors.applicantPhoneNumber, caseData.applicant_details.phone_number);
    I.fillField(applicantDetailsSelectors.applicantEmailAddress, caseData.applicant_details.email);
    I.fillField(applicantDetailsSelectors.applicantDobDay, caseData.applicant_details.dob_day);
    I.fillField(applicantDetailsSelectors.applicantDobMonth, caseData.applicant_details.dob_month);
    I.fillField(applicantDetailsSelectors.applicantDobYear, caseData.applicant_details.dob_year);
    if(contactPreference.toLowerCase() == 'email') {
        I.click(applicantDetailsSelectors.applicantContactPreferenceEmail);
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(applicantDetailsSelectors.applicantContactPreferencePost);
        // I.waitForVisible(addressSelectors.addressPostCodeLookUp, 3);
        this.fillAddressDetails();
    }
  }

  fillRepresentativeDetailsForm(contactPreference: string) {
    I.see(pageHeadings.representative_details_form.subheading, headingsSelectors.h3_heading);
    I.fillField(representativeDetailsSelectors.representativeFullName, caseData.representative_details.full_name);
    I.fillField(representativeDetailsSelectors.representativeOrganisation, caseData.representative_details.organistation);
    I.fillField(representativeDetailsSelectors.representativeContactNumber, caseData.representative_details.contact_number);
    I.fillField(representativeDetailsSelectors.representativeReference, caseData.representative_details.representative_reference);
    I.click(representativeDetailsSelectors.representativeLegallyQualifiedYes);
    if(contactPreference.toLowerCase() == 'email') {
        I.click(representativeDetailsSelectors.representativeContactPreferenceEmail);
        I.waitForVisible(representativeDetailsSelectors.representativeEmailAddress, 1);
        I.fillField(representativeDetailsSelectors.representativeEmailAddress, caseData.representative_details.email);
    } else if(contactPreference.toLowerCase() == 'post') {
        I.click(representativeDetailsSelectors.representativeContactPreferencePost);
        this.fillAddressDetails();
    }
  }

  fillContactPreferencesForm() {
    I.see(pageHeadings.contact_preferences_form.subheading, headingsSelectors.ccd_subheading);
    I.checkOption(contactPreferencesSelectors.subject);
    if(browserHelpers.hasSelector(contactPreferencesSelectors.applicant))
        I.checkOption(contactPreferencesSelectors.applicant);
    if(browserHelpers.hasSelector(contactPreferencesSelectors.representative))
        I.checkOption(contactPreferencesSelectors.representative);    
  }

  async uploadFiles() {
    I.see(pageHeadings.upload_tribunals_form.subheading, headingsSelectors.h1_heading);
    I.click('Add new');
    let filePath = '/resources/documents_and_images/sample_file.jpg';
    I.attachFile(uploadFilesSelectors.chooseFileButton, filePath);
    I.fillField(uploadFilesSelectors.descriptionMultiLineBox, caseData.upload_tribunal_forms.multi_line_box_description);
    I.waitForVisible(uploadFilesSelectors.disabledCancelUploadButton, 30);
  }

  fillFurtherDetailsForm() {
    I.see(pageHeadings.further_details_form.subheading, headingsSelectors.h2_heading);
    I.selectOption(furtherDetailsSelectors.scheme, caseData.further_details.scheme);
    I.click(furtherDetailsSelectors.claimsLodgedWithCicaYes);
    I.fillField(furtherDetailsSelectors.cicaReferenceNumber, caseData.further_details.cica_reference_number);
    I.click(furtherDetailsSelectors.compensationClaimsLinkedYes);
    I.fillField(furtherDetailsSelectors.policaAuthorityManagementIncident, caseData.further_details.police_authority_management_incident);
    I.click(furtherDetailsSelectors.tribunalFormsReceivedYes);
    I.click(furtherDetailsSelectors.missedDeadlineExplainedYes);
  }


  fillAddressDetails () {;
    I.fillField('Enter a UK postcode', 'SW11 1PD');
    I.click('Find address');
    I.selectOption('Select an address', '1 Rse Way, London');
    I.fillField('Address Line 2', 'Test Address Line 2');
    I.fillField('Address Line 3', 'Test Address Line 3');
  }
};

export default new CreateCasePage();