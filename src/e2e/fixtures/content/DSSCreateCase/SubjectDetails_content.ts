const randomLetters = Array.from({ length: 5 }, () =>
  String.fromCharCode(65 + Math.floor(Math.random() * 26)),
).join("");

const subjectDetailsContent = {
  pageTitle: "Who is the subject of this case?",
  hintText1:
    "The subject of a case may be you, or the person who you are submitting this tribunal form on behalf of.",
  subHeading1: "Full name",
  subHeading2: "Date of birth",
  hintText2: "For example, 31 3 1980",
  textOnPage1: "Day",
  textOnPage2: "Month",
  textOnPage3: "Year",
  name: `Subject AutoTesting${randomLetters}`,
  dayOfBirth: "1",
  monthOfBirth: "1",
  yearOfBirth: "2000",
  button: "Continue",
  errorBanner: "There is a problem",
  fullNameError: "Enter full name",
  dateOfBirthError: "Date of birth must include a day, month and year",
  invalidDOBError: "Date of birth must be a real date",
  incompleteDOBError:
    "Date of birth must be a real date and must include a day, month and year",
  pastDOBError: "Date of birth must be after 31/12/1899",
  futureDOBError: "Date of birth must be in the past",
  htmlError: "Full name must not include HTML",

  pageTitleCy: "Pwy yw testun yr achos hwn?",
  hintTextCy1:
    "Gall testun yr achos fod yn chi, neu’r unigolyn rydych yn cyflwyno’r ffurflen tribiwnlys hon ar ei ran.",
  subHeadingCy1: "Enw llawn",
  subHeadingCy2: "Dyddiad geni",
  hintTextCy2: "Er enghraifft, 31 3 1980",
  textOnPageCy1: "Diwrnod",
  textOnPageCy2: "Mis",
  textOnPageCy3: "Blwyddyn",
  buttonCy: "Parhau",
  errorBannerCy: "Mae yna broblem",
  fullNameErrorCy: "Nodwch enw llawn",
  dateOfBirthErrorCy: "Rhaid i'r dyddiad geni gynnwys diwrnod, mis a blwyddyn",
  invalidDOBErrorCy: "Rhaid i'r dyddiad geni fod yn ddyddiad go iawn",
  incompleteDOBErrorCy:
    "Rhaid i'r dyddiad geni fod yn ddyddiad go iawn a rhaid iddo gynnwys diwrnod, mis a blwyddyn",
  pastDOBErrorCy: "Rhaid i'r dyddiad geni fod ar ôl 31/12/1899",
  futureDOBErrorCy: "Rhaid i'r dyddiad geni fod yn y gorffennol",
  htmlErrorCy: "Ni ddylai'r enw llawn gynnwys HTML",
} as const;

export default subjectDetailsContent;
