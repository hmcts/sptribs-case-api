const createSummaryHearingAttendees_content = {
  pageHint: "Hearings: Create summary",
  pageTitle: "Hearing attendees",
  caseReference: "Case number: ",
  textOnPage1: "Which judge heard the case? (Optional)",
  textOnPage2: "Was it a full panel hearing?",
  textOnPage3: "No. It was a 'sit alone' hearing",
  textOnPage4: "Yes",
  subTitle1: "Panel member and Role",
  errorBanner: "There is a problem",
  fullPanelError: "Was it a full panel hearing? is required",
  panelMemberError: "Panel member and Role is required",
  textOnPage5: "Name of the panel member",
  textOnPage6: "What was their role on panel?",
  textOnPage7: "Full member",
  textOnPage8: "Observer",
  textOnPage9: "Appraiser",
  nameError: "Name of the panel member is required",
  roleError: "What was their role on panel? is required",
} as const;

export default createSummaryHearingAttendees_content;
