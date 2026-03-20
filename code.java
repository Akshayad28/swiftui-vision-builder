private static Set<String> executed = new HashSet<>();
private static Set<String> pendingWrite = new HashSet<>();

@After
public void afterScenario(Scenario scenario) {

    String scenarioId = scenario.getUri() + ":" + scenario.getLine();

    // Mark this scenario outline as executed
    pendingWrite.add(scenarioId);
}





@AfterAll
public static void finalWrite() {

    for (String scenarioId : pendingWrite) {

        ExcelWriteClass.writeData(Hooks.tagName);

        System.out.println("📊 Excel created ONCE for: " + scenarioId);
    }

    // Close DB here
    DBConnectionManager.close();
}



@Before
public void setUp(Scenario scenario) {

    String scenarioId = scenario.getUri() + ":" + scenario.getLine();

    String tag = scenario.getSourceTagNames()
            .stream()
            .map(t -> t.replace("@", ""))
            .findFirst()
            .orElse("DefaultTag");

    scenarioTagMap.putIfAbsent(scenarioId, tag);
}






@After
public void afterScenario(Scenario scenario) {

    String scenarioId = scenario.getUri() + ":" + scenario.getLine();

    executed.add(scenarioId);
}



@AfterAll
public static void finalWrite() {

    for (Map.Entry<String, String> entry : scenarioTagMap.entrySet()) {

        String scenarioId = entry.getKey();
        String tag = entry.getValue();

        ExcelWriteClass.writeData(tag);

        System.out.println("📊 Excel created for: " + scenarioId);
    }

    DBConnectionManager.close();
    System.out.println("🔒 DB Closed");
}
