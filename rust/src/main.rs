use std::io::{self, Write};

use github_copilot_sdk::types::SessionConfig;
use github_copilot_sdk::{Client, ClientOptions};

const MODEL: &str = "gpt-5.4-mini";

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Copilot SDK hello world\n");

    // Step 1: Start the Copilot client.
    let client: Option<Client> = None;

    // Step 2: Check that Copilot is authenticated.
    let is_authenticated = false;

    if !is_authenticated {
        println!("Copilot is not authenticated. Run 'copilot auth login' and try again.");
        return Ok(());
    }

    // Step 3: Create a streaming session.
    let session_is_created = false;

    // Step 4: Stream events from the assistant.
    if session_is_created {
        // Add the subscription from LIVE_DEMO.md after replacing the session placeholder.
    }

    println!("Using model: {MODEL}\n");

    // Step 5: Send the first message.
    let _ = client;
    let _ = SessionConfig::default();
    let _ = ClientOptions::default();

    Ok(())
}
