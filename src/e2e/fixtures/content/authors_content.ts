import config from "../../config.ts";

const authors_content = {
  automatedCitizen: "Automated CITIZEN",
  automatedCaseworker: "Automated CASEWORKER",
  automatedSeniorCaseWorker: "Automated SENIORCASEWORKER",
  automatedHearingCentreAdmin: "Automated HEARINGCENTREADMIN",
  automatedHearingCentreTeamLead: "Automated HEARINGCENTRETEAMLEAD",
  demoCitizen: "Dss CITIZEN1",

  assignedUserLO: "sptribswa seniorcaseworker",
  assignedUserAdmin: "sptribswa hearingcentreadmin",
  assignedUserJudge: config.FEBaseURL.includes("demo")
    ? "Mr Logan Everett"
    : "Tribunal Judge John Jones",

  postCode: "SW1A 1AA",
  selectOption: "Buckingham Palace, London",
  buildingAndStreet: "Buckingham Palace",
  townOrCity: "London",
  country: "United Kingdom",
  emailAddress: "AutoTestSubject@mail.com",
} as const;

export default authors_content;
