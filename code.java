import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Scenario;

import java.util.HashSet;
import java.util.Set;

public class Hooks {

    public static String tagName = "DefaultTag";

    // Track execution
    private static Set<String> processedScenarios = new HashSet<>();
    private static String currentScenarioId = "";
    private static boolean shouldWrite = false;

    @Before
    public void setUp(Scenario scenario) {

        // Capture tag
        tagName = scenario.getSourceTagNames()
                .stream()
                .map(tag -> tag.replace("@", ""))
                .findFirst()
                .orElse("DefaultTag");

        // Unique ID for scenario outline
        currentScenarioId = scenario.getUri() + ":" + scenario.getLine();

        // First row → allow writing later
        if (!processedScenarios.contains(currentScenarioId)) {
            shouldWrite = true;
        }
    }

    @After
    public void afterScenario(Scenario scenario) {

        String scenarioId = scenario.getUri() + ":" + scenario.getLine();

        // ❗ ONLY FIRST time → write AFTER full execution
        if (shouldWrite && !processedScenarios.contains(scenarioId)) {

            // 👉 Delay writing until LAST execution using this trick:
            // mark it processed AFTER first detection
            processedScenarios.add(scenarioId);

            // ⚠️ IMPORTANT: DO NOT write immediately
            // Instead, use shutdown hook OR AfterAll
        }
    }

    @AfterAll
    public static void afterAll() {

        // ✅ FINAL WRITE (ONLY ONCE PER SCENARIO OUTLINE)
        for (String scenarioId : processedScenarios) {

            ExcelWriteClass.writeData(tagName);

            System.out.println("📊 Excel created for Scenario Outline: " + scenarioId);
        }

        DBConnectionManager.close();
        System.out.println("🔒 DB Closed");
    }
}
