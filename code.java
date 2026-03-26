@Then("The count should be same for preprod and prod {string}")
public boolean compareCount(String monitor) throws FileNotFoundException {

    // ✅ Headers
    List<String> headers = new ArrayList<>();
    headers.add("ProdMonitorCount");
    headers.add("PreprodMonitorCount");
    headers.add("Monitor");
    headers.add("Difference");
    headers.add("Difference_Percentage");
    headers.add("Match_Status");

    boolean status_flag;
    double percentage = 0.0;
    double difference = Math.abs(prodMonitorCount - preprodMonitorCount);

    // 🔧 Thresholds
    int SMALL_DIFF = 1;
    int MAX_DIFF = 5;
    double MAX_PERCENT = 2.0;

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

        // ✅ Percentage Calculation (PREPROD base)
        percentage = (difference / preprodMonitorCount) * 100;
        percentage = Math.abs(percentage);
        percentage = Math.round(percentage * 100.0) / 100.0;

        // ✅ FINAL FIXED LOGIC
        if (difference <= SMALL_DIFF) {
            status_flag = true;
        }
        else if (difference <= MAX_DIFF && percentage <= MAX_PERCENT) {
            status_flag = true;
        }
        else {
            status_flag = false;
        }

        // ✅ Logging
        if (status_flag) {
            System.out.println("PASS: Monitor " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
            testScenario.log("PASS: Monitor " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
        } else {
            System.out.println("FAIL: Monitor " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
            testScenario.log("FAIL: Monitor " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
        }
    }

    // ✅ Excel Data
    List<String> data = new ArrayList<>();
    data.add(String.valueOf(prodMonitorCount));
    data.add(String.valueOf(preprodMonitorCount));
    data.add(monitor);
    data.add(String.valueOf(difference));
    data.add(String.valueOf(percentage));
    data.add(String.valueOf(status_flag));

    excelWriter.writeExcel(
            "Monitor_Level_Checks",
            headers,
            Collections.singletonList(data)
    );

    // ✅ Assertion
    Assert.assertTrue(
            "Validation failed for monitor: " + monitor +
                    " | Prod=" + prodMonitorCount +
                    " | Preprod=" + preprodMonitorCount +
                    " | Diff=" + difference +
                    " | %=" + percentage,
            status_flag
    );

    return status_flag;
}
