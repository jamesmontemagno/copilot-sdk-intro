package demo;

import com.github.copilot.CopilotClient;
import com.github.copilot.rpc.ModelInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ModelSelector {
    private ModelSelector() {
    }

    public static String select(CopilotClient client, String preferredModel) throws Exception {
        List<ModelInfo> models = client.listModels().get();
        if (models == null || models.isEmpty()) {
            return null;
        }

        int defaultIndex = 0;
        for (int index = 0; index < models.size(); index++) {
            if (models.get(index).getId().equalsIgnoreCase(preferredModel)) {
                defaultIndex = index;
                break;
            }
        }

        System.out.println("Choose a Copilot model:");
        for (int index = 0; index < models.size(); index++) {
            System.out.println("  " + (index + 1) + ". " + models.get(index).getId());
        }

        return models.get(readIndex("Model [" + (defaultIndex + 1) + "]: ", models.size(), defaultIndex)).getId();
    }

    static int readIndex(String prompt, int optionCount, int defaultIndex) throws Exception {
        var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            System.out.print(prompt);
            String answer = reader.readLine();
            if (answer == null || answer.isBlank()) {
                return defaultIndex;
            }
            try {
                int selection = Integer.parseInt(answer.trim());
                if (selection >= 1 && selection <= optionCount) {
                    return selection - 1;
                }
            } catch (NumberFormatException ignored) {
                // Ask again below.
            }
            System.out.println("Enter a number from 1 to " + optionCount + ".");
        }
    }
}
