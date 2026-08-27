from copilot.rpc import PermissionDecisionApproveOnce, PermissionDecisionReject


def permission_prompt(request, _invocation):
    if getattr(request, "kind", None) != "tool":
        return PermissionDecisionReject(
            feedback="This demo only permits its GitHub Podcast episode lookup tool."
        )

    tool_name = getattr(request, "tool_name", "custom tool")
    answer = input(f"Approve {tool_name}? [y/N] ").strip()
    if answer.lower() == "y":
        return PermissionDecisionApproveOnce()
    return PermissionDecisionReject(feedback="The user did not approve the episode lookup.")
