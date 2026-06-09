const decisionUpload_content = {
  pageHint: "Decision: Issue final decision",
  pageTitle: "Upload decision notice",
  caseReference: "Case number: ",
  textOnPage1:
    "Upload a copy of the decision notice that you want to add to this case.",
  subTitle1: "The decision notice should be:",
  textOnPage2: "a maximum of 100MB in size (larger files must be split)",
  textOnPage3: "labelled clearly, e.g. applicant-name-decision-notice.pdf",
  textOnPage4:
    "Note: If the remove button is disabled, please refresh the page to remove attachments",
  subTitle2: "File Attachments",
  textOnPage5: "Description",
  textOnPage6: "File",

  description: "Lorem ipsum description",

  errorBanner: "There is a problem",
  errorNoEntryDescription: "Description is required",
  errorNoEntryFile: "Select or fill the required File field",
  errorInvalidFile: "Error Uploading File",
} as const;

export default decisionUpload_content;
