import config from "../../config.ts";

const waUsers_content = {
  userRoleAdmin: "waHearingCentreAdmin",
  userRoleCitizen: config.FEBaseURL.includes("demo")
    ? "demoCitizen"
    : "citizen",
  userRoleLO: "waSeniorCaseworker",
  userRoleJudge: "waPresidentOfTribunal",
  userRoleCaseWorker: "waCaseWorker",
} as const;

export default waUsers_content;
