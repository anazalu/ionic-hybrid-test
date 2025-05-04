// src/test/java/RunCucumberTest.java
// package runners; // Uncomment or set correct package if needed

// REMOVE: import base.BaseTest;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/java/resources/features",
    glue = "stepdefinitions", // Make sure this points to where Hooks.java is
    plugin = {
        "pretty",
        "html:target/cucumber-reports.html",
        "json:target/cucumber.json"
    },
    monochrome = true
)
// NO LONGER extends BaseTest
public class RunCucumberTest extends AbstractTestNGCucumberTests {
    // Body is likely empty now
}


/* 

// package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/java/resources/features",
    glue = "stepdefinitions",
    plugin = {
        "pretty",
        "html:target/cucumber-reports.html",
        "json:target/cucumber.json"
    },
    monochrome = true
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {}
*/
