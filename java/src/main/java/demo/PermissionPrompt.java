package demo;

import com.github.copilot.rpc.PermissionHandler;
import com.github.copilot.rpc.PermissionInvocation;
import com.github.copilot.rpc.PermissionRequest;
import com.github.copilot.rpc.PermissionRequestResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Keeps the presenter as the approval point. Anything that is not this demo's own
 * local tool is denied, and a tool call must be approved on stdin.
 */
public final class PermissionPrompt {
    public static final PermissionHandler HANDLER = PermissionPrompt::request;

    private PermissionPrompt() {
    }

    private static CompletableFuture<PermissionRequestResult> request(
            PermissionRequest request, PermissionInvocation invocation) {
        if (!"custom-tool".equals(request.getKind())) {
            return CompletableFuture.completedFuture(PermissionRequestResult.reject(
                    "This demo only permits its GitHub Podcast episode lookup tool."));
        }

        Map<String, Object> details = request.getExtensionData();
        Object toolName = details == null ? null : details.get("toolName");
        System.out.print("Approve " + (toolName == null ? "custom tool" : toolName) + "? [y/N] ");

        String answer;
        try {
            answer = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
        } catch (Exception failure) {
            answer = null;
        }

        return CompletableFuture.completedFuture(
                answer != null && answer.trim().equalsIgnoreCase("y")
                        ? PermissionRequestResult.approveOnce()
                        : PermissionRequestResult.reject("The user did not approve the episode lookup."));
    }
}
