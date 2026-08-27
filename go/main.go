package main

import (
	"context"
	"fmt"
	"sync/atomic"

	copilot "github.com/github/copilot-sdk/go"
)

const preferredModel = "gpt-5.4-mini"

func main() {
	fmt.Println("Copilot SDK hello world")

	// Step 1: Start the Copilot client.
	var client *copilot.Client
	_ = client

	// Step 2: Check that Copilot is authenticated.
	isAuthenticated := false

	if !isAuthenticated {
		fmt.Println("Copilot is not authenticated. Run 'copilot auth login' and try again.")
		return
	}

	// Step 3: Create a streaming session.
	var session *copilot.Session

	// Step 4: Stream events from the assistant.
	if session != nil {
		var receivedDelta atomic.Bool
		unsubscribe := session.On(func(event copilot.SessionEvent) {
			switch data := event.Data.(type) {
			case *copilot.AssistantMessageDeltaData:
				if data.DeltaContent != "" {
					receivedDelta.Store(true)
					fmt.Print(data.DeltaContent)
				}
			case *copilot.ToolExecutionStartData:
				fmt.Printf("[Tool call started] %s\n", data.ToolName)
			case *copilot.ToolExecutionCompleteData:
				fmt.Println("\n[Tool call complete]")
			case *copilot.AssistantMessageData:
				if !receivedDelta.Load() {
					fmt.Print(data.Content)
				}
			}
		})
		defer unsubscribe()
	}

	fmt.Printf("Using model: %s\n", preferredModel)

	// Step 5: Send the first message.
	_ = context.Background()
}
