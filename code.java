private static Set<String> executed = new HashSet<>();

@After
public void afterScenario(Scenario scenario) {

    String scenarioId = scenario.getUri() + ":" + scenario.getLine();

    if (!executed.contains(scenarioId)) {

        ExcelWriteClass.writeData(tagName);

        executed.add(scenarioId);

        System.out.println("📊 Excel created ONCE for scenario outline");
    }
}
