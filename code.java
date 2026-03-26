@Then("The count should be same for preprod and prod {string}")
public boolean compareCount(String monitor) throws FileNotFoundException {

    // ✅ Headers
    List<String> headers = new ArrayList<>();
    headers.add("ProdMonitorCount");
    headers.add("PreprodMonitorCount");
    headers.add("Monitor");
    headers.add("Difference_Percentage");
    headers.add("Match_Status");

    boolean status_flag;
    double percentage = 0.0;

    // ✅ ZERO HANDLING
    if (preprodMonitorCount == 0 || prodMonitorCount == 0) {

        if (preprodMonitorCount == 0 && prodMonitorCount == 0) {
            status_flag = true;
        } else {
            status_flag = false;
        }

    } else {

        // ✅ Percentage Calculation (PREPROD base)
        double difference = Math.abs(prodMonitorCount - preprodMonitorCount);
        percentage = (difference / preprodMonitorCount) * 100;

        percentage = Math.abs(percentage);
        percentage = Math.round(percentage * 100.0) / 100.0;

        // 🔥 FINAL LOGIC (ONLY %)
        if (percentage <= 2) {
            status_flag = true;
            System.out.println("PASS: Monitor " + monitor + " | %=" + percentage);
            testScenario.log("PASS: Monitor " + monitor + " | %=" + percentage);
        } else {
            status_flag = false;
            System.out.println("FAIL: Monitor " + monitor + " | %=" + percentage);
            testScenario.log("FAIL: Monitor " + monitor + " | %=" + percentage);
        }
    }

    // ✅ Excel Data
    List<String> data = new ArrayList<>();
    data.add(String.valueOf(prodMonitorCount));
    data.add(String.valueOf(preprodMonitorCount));
    data.add(monitor);
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
                    " | %=" + percentage,
            status_flag
    );

    return status_flag;
}
