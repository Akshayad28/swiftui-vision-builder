package com.barclays.testautomation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.TestNG;

import java.util.Arrays;

@CucumberOptions(
        features = "src/main/resources/features",
        glue = "com.barclays.testautomation",
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber-report.json"
        },
        monochrome = true
)
public class RunnerIT_TestNG extends AbstractTestNGCucumberTests {

    public static void main(String[] args) {

        TestNG testng = new TestNG();
        testng.setTestSuites(Arrays.asList("testng.xml"));
        testng.run();

    }

}
