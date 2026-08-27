import { CopilotClient, type CopilotSession } from "@github/copilot-sdk";

const model = "gpt-5.4-mini";

console.log("Copilot SDK hello world\n");

// Step 1: Start the Copilot client.
let client: CopilotClient;

// Step 2: Check that Copilot is authenticated.
const isAuthenticated = false;

if (!isAuthenticated) {
  console.log("Copilot is not authenticated. Run 'copilot auth login' and try again.");
  process.exit(0);
}

// Step 3: Create a streaming session.
let session: CopilotSession;

// Step 4: Stream events from the assistant.
session!.on((event) => {
  if (event.type === "assistant.message_delta" && event.data.deltaContent) {
    process.stdout.write(event.data.deltaContent);
  } else if (event.type === "tool.execution_start") {
    console.log(`[Tool call started] ${event.data.toolName} ${JSON.stringify(event.data.arguments ?? {})}`);
  } else if (event.type === "tool.execution_complete") {
    console.log("\n[Tool call complete]\n");
  } else if (event.type === "session.error") {
    throw new Error(event.data.message);
  }
});

console.log(`Using model: ${model}\n`);

// Step 5: Send the first message.
