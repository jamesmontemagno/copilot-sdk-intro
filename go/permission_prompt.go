package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	copilot "github.com/github/copilot-sdk/go"
	"github.com/github/copilot-sdk/go/rpc"
)

// permissionPrompt keeps the presenter as the approval point. Anything that is not
// this demo's own local tool is denied, and a tool call must be approved on stdin.
func permissionPrompt(request copilot.PermissionRequest, _ copilot.PermissionInvocation) (rpc.PermissionDecision, error) {
	tool, isCustomTool := request.(*copilot.PermissionRequestCustomTool)
	if !isCustomTool {
		return rejectPermission("This demo only permits its GitHub Podcast episode lookup tool."), nil
	}

	fmt.Printf("Approve %s? [y/N] ", tool.ToolName)
	answer, _ := bufio.NewReader(os.Stdin).ReadString('\n')
	if strings.EqualFold(strings.TrimSpace(answer), "y") {
		return &rpc.PermissionDecisionApproveOnce{}, nil
	}
	return rejectPermission("The user did not approve the episode lookup."), nil
}

func rejectPermission(feedback string) rpc.PermissionDecision {
	return &rpc.PermissionDecisionReject{Feedback: &feedback}
}
