@Then("The preprod count should match with prod count for ProductId {string}")
public boolean compareCount(String ProductId) throws FileNotFoundException {

    // ✅ Headers
    List<String> headers = new ArrayList<>();
    headers.add("ProdCount");
    headers.add("PreprodCount");
    headers.add("ProductId");
    headers.add("Difference_Percentage");
    headers.add("Match_Status");

    boolean status_flag;
    double percentage = 0.0;

    // ✅ ZERO HANDLING
    if (preprodCount == 0 || prodCount == 0) {

        if (preprodCount == 0 && prodCount == 0) {
            status_flag = true;
        } else {
            status_flag = false;
        }

    } else {

        // ✅ Percentage Calculation (PREPROD base)
        double difference = Math.abs(prodCount - preprodCount);
        percentage = (difference / preprodCount) * 100;

        percentage = Math.abs(percentage);
        percentage = Math.round(percentage * 100.0) / 100.0;

        // 🔥 FINAL LOGIC (ONLY %)
        if (percentage <= 2) {
            status_flag = true;
        } else {
            status_flag = false;
        }

        // ✅ Logging
        if (status_flag) {
            System.out.println("PASS: Product " + ProductId +
                    " | %=" + percentage);
            testScenario.log("PASS: Product " + ProductId +
                    " | %=" + percentage);
        } else {
            System.out.println("FAIL: Product " + ProductId +
                    " | %=" + percentage);
            testScenario.log("FAIL: Product " + ProductId +
                    " | %=" + percentage);
        }
    }

    // ✅ Excel Data
    List<List<String>> data = new ArrayList<>();
    List<String> row = new ArrayList<>();

    row.add(String.valueOf(prodCount));
    row.add(String.valueOf(preprodCount));
    row.add(ProductId);
    row.add(String.valueOf(percentage));
    row.add(String.valueOf(status_flag));

    data.add(row);

    // ✅ Write Excel
    excelWriter.writeExcel(
            "Product_Mapping",
            headers,
            data
    );

    // ✅ Assertion
    Assert.assertTrue(
            "Validation failed for Product: " + ProductId +
                    " | Prod=" + prodCount +
                    " | Preprod=" + preprodCount +
                    " | %=" + percentage,
            status_flag
    );

    return status_flag;
}
