const editDueDate_content = {
  pageHint: "Orders: Manage due date",
  pageTitle: "Amend due dates",

  caseReference: "Case number: ",

  subTitle1: "Due Date",
  subTitle2: "Due Date",
  subTitle3: "Due Date 2",

  textOnPage1: "14 days",
  textOnPage2: "21 days",
  textOnPage3: "28 days",
  textOnPage4: "120 days",
  textOnPage5: "Other",
  textOnPage6: "Completed (Optional)",
  textOnPage7: "Yes",

  errorBanner: " There is a problem ",
  errorBlank1: " Field is not valid ",
  errorBlank2: " The data entered is not valid for Field ",

  day: "2",
  month: "2",
  year: `${new Date().getFullYear() + 1}`,
  information: "Updated Optional Information",
} as const;

export default editDueDate_content;
