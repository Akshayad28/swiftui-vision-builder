@Then("The count should be same for preprod and prod {string}")
public boolean compareCount(String monitor) throws FileNotFoundException {

    // ✅ Headers
    List<String> headers = new ArrayList<>();
    headers.add("ProdMonitorCount");
    headers.add("PreprodMonitorCount");
    headers.add("Monitor");
    headers.add("Match_Status");
    headers.add("Difference_Percentage");

    boolean status_flag;
    double percentage = 0.0;

    // ✅ ZERO HANDLING
    if (preprodMonitorCount == 0 || prodMonitorCount == 0) {

        if (preprodMonitorCount == 0 && prodMonitorCount == 0) {
            status_flag = true;
            System.out.println("Both values are 0 for monitor: " + monitor);
            testScenario.log("Both values are 0 for monitor: " + monitor);
        } else {
            status_flag = false;
            System.out.println("One of the values is 0 for monitor: " + monitor);
            testScenario.log("One of the values is 0 for monitor: " + monitor);
        }

    } else {

        // ✅ YOUR FORMULA (PREPROD BASE)
        percentage = ((double)(prodMonitorCount - preprodMonitorCount) / preprodMonitorCount) * 100;

        // ✅ Make absolute
        percentage = Math.abs(percentage);

        // ✅ Optional rounding
        percentage = Math.round(percentage * 100.0) / 100.0;

        if (percentage <= 2) {
            status_flag = true;
            System.out.println("Count within 2% tolerance for monitor: " + monitor + " | Diff% = " + percentage);
            testScenario.log("PASS: Within 2% tolerance for monitor: " + monitor + " | Diff% = " + percentage);
        } else {
            status_flag = false;
            System.out.println("Count exceeds 2% tolerance for monitor: " + monitor + " | Diff% = " + percentage);
            testScenario.log("FAIL: Exceeds 2% tolerance for monitor: " + monitor + " | Diff% = " + percentage);
        }
    }

    // ✅ Prepare Excel Data
    List<String> data = new ArrayList<>();
    data.add(String.valueOf(prodMonitorCount));
    data.add(String.valueOf(preprodMonitorCount));
    data.add(monitor);
    data.add(String.valueOf(status_flag));
    data.add(String.valueOf(percentage));

    // ✅ Write to Excel
    excelWriter.writeExcel(
            "Monitor_Level_Checks",
            headers,
            Collections.singletonList(data)
    );

    // ✅ Assertion
    Assert.assertTrue(status_flag, "Validation failed for monitor: " + monitor);

    return status_flag;
}
