const submit_content = {
  pageHint: "Orders: Send order",
  pageTitle: "Check your answers",
  caseReference: "Case number: ",
  textOnPage1: "Check the information below carefully.",
  textOnPage2: "How would you like to issue an order?",

  draft: "Issue and send an existing draft",
  draft1: "Order to be sent",

  upload: "Upload a new order from your computer",
  upload1: "Upload a file to the system",
  upload2: "Upload a file to the system 1",
  upload3: "Description",
  upload4: "File",

  dueDate1: "Due Date",
  dueDate2: "Due Date 1",
  dueDate3: "Due Date information",
  dueDate4: "Completed",

  textOnPage3: "Order information recipient",

  reminder1:
    "Should a reminder notification be sent? You can only send a reminder for the earliest due date stated on this order",
  reminder2:
    "How many days before the earliest due date should a reminder be sent?",
  reminderDay: " day",
  reminderDays: " days",
} as const;

export default submit_content;
