private static String extractOutlineName(Scenario scenario) {
    try {
        String rawName = scenario.getName();

        // ✅ Null check on raw name
        if (rawName == null || rawName.trim().isEmpty()) {
            return "UnknownScenario";
        }

        // Cucumber appends example values to outline name like:
        // "To verify completeness monitor=Monitor-India TCID=MLTC001"
        // We want just: "To verify completeness"

        // ✅ Try splitting on first param pattern (word=value)
        String[] parts = rawName.split("\\s+\\w+=");
        if (parts.length > 0 && parts[0] != null 
                && !parts[0].trim().isEmpty()) {
            return parts[0].trim();
        }

        // ✅ Fallback — return full name if no param pattern found
        return rawName.trim();

    } catch (Exception e) {
        // ✅ Safety net — never let this crash the test run
        return "UnknownScenario";
    }
}


@Before
public void setup(Scenario scenario) {
    this.testScenario = scenario;

    String incomingOutlineName = extractOutlineName(scenario);

    // ✅ Add this temporarily to debug what name is being extracted
    System.out.println("Raw scenario name   : " + scenario.getName());
    System.out.println("Extracted outline   : " + incomingOutlineName);

    // Outline changed — write previous workbook
    if (currentOutlineName != null
            && !currentOutlineName.equals(incomingOutlineName)) {

        System.out.println("Outline changed from [" + currentOutlineName 
                + "] to [" + incomingOutlineName + "] — writing Excel");

        ExcelWriteClass.writeData(currentOutlineName);
        ExcelWriteClass.workbook = null;
    }

    // Create new workbook if null
    if (ExcelWriteClass.workbook == null) {
        ExcelWriteClass.workbook = new XSSFWorkbook();
        currentOutlineName = incomingOutlineName;
    }

    // ... rest of your DB connection code
}
