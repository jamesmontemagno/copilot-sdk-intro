import type { CopilotClient } from "@github/copilot-sdk";
import { createInterface } from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";

export async function selectModel(client: CopilotClient, preferredModel: string): Promise<string | undefined> {
  const models = await client.listModels();
  if (models.length === 0) {
    return undefined;
  }

  const preferredIndex = models.findIndex((model) => model.id.toLowerCase() === preferredModel.toLowerCase());
  const defaultIndex = preferredIndex >= 0 ? preferredIndex : 0;

  console.log("Choose a Copilot model:");
  models.forEach((model, index) => console.log(`  ${index + 1}. ${model.id}`));

  const selectedIndex = await readIndex(`Model [${defaultIndex + 1}]: `, models.length, defaultIndex);
  return models[selectedIndex]?.id;
}

export async function readIndex(prompt: string, optionCount: number, defaultIndex: number): Promise<number> {
  const rl = createInterface({ input, output });
  try {
    while (true) {
      const answer = (await rl.question(prompt)).trim();
      if (!answer) {
        return defaultIndex;
      }

      const selection = Number.parseInt(answer, 10);
      if (Number.isInteger(selection) && selection >= 1 && selection <= optionCount) {
        return selection - 1;
      }

      console.log(`Enter a number from 1 to ${optionCount}.`);
    }
  } finally {
    rl.close();
  }
}
