import type { PermissionHandler } from "@github/copilot-sdk";
import { createInterface } from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";

export const permissionPrompt: PermissionHandler = async (request) => {
  if (request.kind !== "custom-tool") {
    return {
      kind: "reject",
      feedback: "This demo only permits its Merge Conflict episode lookup tool.",
    };
  }

  const toolName = request.toolName;
  const rl = createInterface({ input, output });
  try {
    const answer = (await rl.question(`Approve ${toolName}? [y/N] `)).trim();
    return answer.toLowerCase() === "y"
      ? { kind: "approve-once" }
      : { kind: "reject", feedback: "The user did not approve the episode lookup." };
  } finally {
    rl.close();
  }
};
