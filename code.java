import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    public static String scenarioName = "DefaultScenario";

    @Before
    public void captureScenario(Scenario testScenario) {

        scenarioName = testScenario.getName()
                .replaceAll("[\\\\/:*?\"<>|]", "_") // clean invalid chars
                .trim();

        System.out.println("📌 Captured Scenario: " + scenarioName);
    }
}
