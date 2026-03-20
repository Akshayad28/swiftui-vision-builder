import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

import java.util.HashMap;
import java.util.Map;

public class Hooks {

    private static Map<String, Integer> executionCount = new HashMap<>();
    private static Map<String, Integer> expectedCount = new HashMap<>();
    private static Map<String, String> scenarioTagMap = new HashMap<>();

    @Before
    public void beforeScenario(Scenario scenario) {

        String scenarioId = scenario.getUri() + ":" + scenario.getLine();

        // Count executions
        int count = executionCount.getOrDefault(scenarioId, 0) + 1;
        executionCount.put(scenarioId, count);

        // Expected count auto-adjust
        expectedCount.put(scenarioId, count);

        // Capture tag (ONLY ONCE)
        if (!scenarioTagMap.containsKey(scenarioId)) {

            String tagName = scenario.getSourceTagNames()
                    .stream()
                    .map(tag -> tag.replace("@", ""))
                    .reduce((a, b) -> a + "_" + b) // multiple tags
                    .orElse("DefaultTag");

            scenarioTagMap.put(scenarioId, tagName);
        }
    }

    @After
    public void afterScenario(Scenario scenario) {

        String scenarioId = scenario.getUri() + ":" + scenario.getLine();

        int executed = executionCount.get(scenarioId);
        int expected = expectedCount.get(scenarioId);

        // ✅ ONLY LAST ROW OF SCENARIO OUTLINE
        if (executed == expected) {

            String tagName = scenarioTagMap.get(scenarioId);

            ExcelWriteClass.writeData(tagName);

            System.out.println("📊 Excel created for: " + tagName);

            // Cleanup for next scenario
            executionCount.remove(scenarioId);
            expectedCount.remove(scenarioId);
            scenarioTagMap.remove(scenarioId);
        }
    }
}
