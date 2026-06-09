import dotenv from "dotenv";
import path from "path";

dotenv.config();

export interface UserCredentials {
  readonly email: string;
  readonly password: string;
}

export type UserRole =
  | "respondent"
  | "citizen"
  | "superUser"
  | "demoCitizen"
  | "waCaseWorker"
  | "waSeniorCaseworker"
  | "waHearingCentreAdmin"
  | "waHearingCentreTeamLead"
  | "waRegionalHearingCentreAdmin"
  | "waRegionalHearingCentreTeamLead"
  | "waCTSCAdmin"
  | "waCTSCTeamLead"
  | "waRespondent"
  | "waPresidentOfTribunal"
  | "waTribunalJudgeSalaried"
  | "waTribunalJudgeFeePaid";

interface Config {
  [key: string]: UserCredentials | string | boolean;
}

const config: Config = {
  respondent: {
    email: process.env.RESPONDENT_USERNAME || "respondent-user",
    password: process.env.RESPONDENT_PASSWORD || "respondent-password",
  },
  citizen: {
    email: process.env.CITIZEN_USERNAME || "citizen-user",
    password: process.env.CITIZEN_PASSWORD || "citizen-password",
  },
  superUser: {
    email: process.env.SUPER_USER_USERNAME || "superUser-user",
    password: process.env.SUPER_USER_PASSWORD || "superUser-password",
  },
  demoCitizen: {
    email: process.env.DEMO_CITIZEN_USERNAME || "demoCitizen-user",
    password: process.env.DEMO_CITIZEN_PASSWORD || "demoCitizen-password",
  },
  waCaseWorker: {
    email: process.env.WA_CASEWORKER_USERNAME || "wa-caseworker-username",
    password: process.env.WA_CASEWORKER_PASSWORD || "wa-caseworker-password",
  },
  waSeniorCaseworker: {
    email:
      process.env.WA_SENIOR_CASEWORKER_USERNAME ||
      "wa-seniorCaseworker-username",
    password:
      process.env.WA_SENIOR_CASEWORKER_PASSWORD ||
      "wa-seniorCaseworker-password",
  },
  waHearingCentreAdmin: {
    email:
      process.env.WA_HEARING_CENTRE_ADMIN_USERNAME ||
      "wa-hearingCentreAdmin-username",
    password:
      process.env.WA_HEARING_CENTRE_ADMIN_PASSWORD ||
      "wa-hearingCentreAdmin-password",
  },
  waHearingCentreTeamLead: {
    email:
      process.env.WA_HEARING_CENTRE_TEAM_LEAD_USERNAME ||
      "wa-hearingCentreTeamLead-username",
    password:
      process.env.WA_HEARING_CENTRE_TEAM_LEAD_PASSWORD ||
      "wa-hearingCentreTeamLead-password",
  },
  waRegionalHearingCentreAdmin: {
    email:
      process.env.WA_REGIONAL_HEARING_CENTRE_ADMIN_USERNAME ||
      "wa-regionalHearingCentreAdmin-username",
    password:
      process.env.WA_REGIONAL_HEARING_CENTRE_ADMIN_PASSWORD ||
      "wa-regionalHearingCentreAdmin-password",
  },
  waRegionalHearingCentreTeamLead: {
    email:
      process.env.WA_REGIONAL_HEARING_CENTRE_TEAM_LEAD_USERNAME ||
      "wa-regionalHearingCentreTeamLead-username",
    password:
      process.env.WA_REGIONAL_HEARING_CENTRE_TEAM_LEAD_PASSWORD ||
      "wa-regionalHearingCentreTeamLead-password",
  },
  waCTSCAdmin: {
    email: process.env.WA_CTSC_ADMIN_USERNAME || "wa-ctscAdmin-username",
    password: process.env.WA_CTSC_ADMIN_PASSWORD || "wa-ctscAdmin-password",
  },
  waCTSCTeamLead: {
    email: process.env.WA_CTSC_TEAM_LEAD_USERNAME || "wa-ctscTeamLead-username",
    password:
      process.env.WA_CTSC_TEAM_LEAD_PASSWORD || "wa-ctscTeamLead-password",
  },
  waRespondent: {
    email: process.env.WA_RESPONDENT_USERNAME || "wa-respondent-username",
    password: process.env.WA_RESPONDENT_PASSWORD || "wa-respondent-password",
  },
  waPresidentOfTribunal: {
    email:
      process.env.WA_PRESIDENT_OF_TRIBUNAL_USERNAME ||
      "wa-presidentOfTribunal-username",
    password:
      process.env.WA_PRESIDENT_OF_TRIBUNAL_PASSWORD ||
      "wa-presidentOfTribunal-password",
  },
  waTribunalJudgeSalaried: {
    email:
      process.env.WA_TRIBUNAL_JUDGE_SALARIED_USERNAME ||
      "wa-tribunalJudgeSalaried-username",
    password:
      process.env.WA_TRIBUNAL_JUDGE_SALARIED_PASSWORD ||
      "wa-tribunalJudgeSalaried-password",
  },
  waTribunalJudgeFeePaid: {
    email:
      process.env.WA_TRIBUNAL_JUDGE_FEE_PAID_USERNAME ||
      "wa-tribunalJudgeFeePaid-username",
    password:
      process.env.WA_TRIBUNAL_JUDGE_FEE_PAID_PASSWORD ||
      "wa-tribunalJudgeFeePaid-password",
  },

  FEBaseURL: process.env.DSS_BASE_URL || "FEBaseURL",
  CaseAPIBaseURL: process.env.CASEAPI_BASE_URL || "CaseAPIBaseURL",
  UpdateCaseBaseURL: process.env.UC_BASE_URL || "UpdateCaseBaseURL",
  skipDSSCreateTests: process.env.skipDSSCreateTests || false,

  testFile: path.resolve(__dirname, "../e2e/fixtures/testFiles/mockFile.txt"),
  testPdfFile: path.resolve(
    __dirname,
    "../e2e/fixtures/testFiles/mockFile.pdf",
  ),
  testWordFile: path.resolve(
    __dirname,
    "../e2e/fixtures/testFiles/mockFile.docx",
  ),
  testOdtFile: path.resolve(
    __dirname,
    "../e2e/fixtures/testFiles/mockFile.odt",
  ),
  testMP3File: path.resolve(
    __dirname,
    "../e2e/fixtures/testFiles/mockFile.mp3",
  ),
};

export default config as {
  [key in UserRole]: UserCredentials;
} & {
  FEBaseURL: string;
  CaseAPIBaseURL: string;
  UpdateCaseBaseURL: string;
  testFile: string;
  testPdfFile: string;
  testWordFile: string;
  testOdtFile: string;
  testMP3File: string;
  skipDSSCreateTests: boolean;
};
