const sendReminder_content = {
  pageHint: "Orders: Send order",
  pageTitle: "Upload an order",
  caseReference: "Case number: ",
  textOnPage1:
    "Should a reminder notification be sent? You can only send a reminder for the earliest due date stated on this order",
  textOnPage2: "Yes",
  textOnPage3: "No",
  textOnPage4:
    "How many days before the earliest due date should a reminder be sent?",
  textOnPage5: "1 day",
  textOnPage6: "3 days",
  textOnPage7: "5 days",
  textOnPage8: "7 days",

  errorBanner: "There is a problem",
  errorNoInput:
    "Should a reminder notification be sent? You can only send a reminder for the earliest due date stated on this order is required",
  errorNoDay:
    "How many days before the earliest due date should a reminder be sent? is required",
} as const;

export default sendReminder_content;
