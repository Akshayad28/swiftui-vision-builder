@Then("The count should be same for preprod and prod {string}")
public boolean compareCount(String monitor) throws FileNotFoundException {

    // ✅ Headers for Excel
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
        percentage = (difference / preprodMonitorCount) * 100;

        // ✅ Make absolute & round
        percentage = Math.abs(percentage);
        percentage = Math.round(percentage * 100.0) / 100.0;

        // ✅ HYBRID LOGIC
        if (difference <= 1 || percentage <= 2) {
            status_flag = true;
            System.out.println("PASS: Within tolerance for monitor: " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
            testScenario.log("PASS: Within tolerance for monitor: " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
        } else {
            status_flag = false;
            System.out.println("FAIL: Exceeds tolerance for monitor: " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
            testScenario.log("FAIL: Exceeds tolerance for monitor: " + monitor +
                    " | Diff=" + difference + " | %=" + percentage);
        }
    }

    // ✅ Prepare Excel Data
    List<String> data = new ArrayList<>();
    data.add(String.valueOf(prodMonitorCount));
    data.add(String.valueOf(preprodMonitorCount));
    data.add(monitor);
    data.add(String.valueOf(difference));
    data.add(String.valueOf(percentage));
    data.add(String.valueOf(status_flag));

    // ✅ Write to Excel
    excelWriter.writeExcel(
            "Monitor_Level_Checks",
            headers,
            Collections.singletonList(data)
    );

    // ✅ Assertion (FIXED ORDER)
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
