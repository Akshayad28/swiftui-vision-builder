@Then("The preprod count should match with prod count for ProductId {string}")
public boolean compareCount(String ProductId) throws FileNotFoundException {

    // ✅ Headers
    List<String> headers = new ArrayList<>();
    headers.add("ProdCount");
    headers.add("PreprodCount");
    headers.add("ProductId");
    headers.add("Difference");
    headers.add("Difference_Percentage");
    headers.add("Match_Status");

    boolean status_flag;
    double percentage = 0.0;
    double difference = Math.abs(prodCount - preprodCount);

    // ✅ ZERO HANDLING
    if (preprodCount == 0 || prodCount == 0) {

        if (preprodCount == 0 && prodCount == 0) {
            status_flag = true;
            System.out.println("Both values are 0 for Product: " + ProductId);
            testScenario.log("Both values are 0 for Product: " + ProductId);
        } else {
            status_flag = false;
            System.out.println("One of the values is 0 for Product: " + ProductId);
            testScenario.log("One of the values is 0 for Product: " + ProductId);
        }

    } else {

        // ✅ PERCENTAGE CALCULATION (PREPROD BASE)
        percentage = (difference / preprodCount) * 100;

        // ✅ absolute + rounding
        percentage = Math.abs(percentage);
        percentage = Math.round(percentage * 100.0) / 100.0;

        // ✅ HYBRID LOGIC
        if (difference <= 1 || percentage <= 2) {
            status_flag = true;
            System.out.println("PASS: Within tolerance for Product: " + ProductId +
                    " | Diff=" + difference + " | %=" + percentage);
            testScenario.log("PASS: Within tolerance for Product: " + ProductId +
                    " | Diff=" + difference + " | %=" + percentage);
        } else {
            status_flag = false;
            System.out.println("FAIL: Exceeds tolerance for Product: " + ProductId +
                    " | Diff=" + difference + " | %=" + percentage);
            testScenario.log("FAIL: Exceeds tolerance for Product: " + ProductId +
                    " | Diff=" + difference + " | %=" + percentage);
        }
    }

    // ✅ Prepare Excel Data
    List<List<String>> data = new ArrayList<>();
    List<String> row = new ArrayList<>();

    row.add(String.valueOf(prodCount));
    row.add(String.valueOf(preprodCount));
    row.add(ProductId);
    row.add(String.valueOf(difference));
    row.add(String.valueOf(percentage));
    row.add(String.valueOf(status_flag));

    data.add(row);

    // ✅ Write Excel
    excelWriter.writeExcel(
            "Product_Mapping",
            headers,
            data
    );

    // ✅ Assertion (FIXED)
    Assert.assertTrue(
            "Validation failed for Product: " + ProductId +
                    " | Prod=" + prodCount +
                    " | Preprod=" + preprodCount +
                    " | Diff=" + difference +
                    " | %=" + percentage,
            status_flag
    );

    return status_flag;
}
