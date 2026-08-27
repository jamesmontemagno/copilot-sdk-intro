package demo;

import com.github.copilot.CopilotClient;
import com.github.copilot.rpc.SessionConfig;

public final class CopilotSdkLiveDemo {
    private static final String MODEL = "gpt-5.4-mini";

    private CopilotSdkLiveDemo() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Copilot SDK hello world\n");

        // Step 1: Start the Copilot client.
        CopilotClient client;

        // Step 2: Check that Copilot is authenticated.
        boolean isAuthenticated = false;

        if (!isAuthenticated) {
            System.out.println("Copilot is not authenticated. Run 'copilot auth login' and try again.");
            return;
        }

        // Step 3: Create a streaming session.
        Object session = null;

        // Step 4: Stream events from the assistant.
        // Add AssistantMessageDeltaEvent, ToolExecutionStartEvent, and ToolExecutionCompleteEvent
        // subscriptions after replacing the session placeholder in the walkthrough.

        System.out.println("Using model: " + MODEL + "\n");

        // Step 5: Send the first message.
        var config = new SessionConfig();
        if (session != null) {
            throw new IllegalStateException("Replace the starter placeholders before running the live demo.");
        }
    }
}
