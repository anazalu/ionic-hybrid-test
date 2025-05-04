package stepdefinitions;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;

import reports.ExtentManager;
import support.DriverMethods;

import base.BaseTest;

import java.net.MalformedURLException;

public class Hooks {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @BeforeAll
    public static void initializeTestRun() throws MalformedURLException {
        System.out.println("--- Hooks @BeforeAll: Initializing Test Run ---");
        // BaseTest.loadPropertiesLogic();
        BaseTest.startServerLogic();
        System.out.println("--- Hooks @BeforeAll: Initialization Complete ---");
    }

    @Before
    public void beforeScenario(Scenario scenario) throws MalformedURLException {
        System.out.println("--- Hooks @Before Scenario: " + scenario.getName() + " ---");
        
        BaseTest.setupDriverLogic();
        extent = ExtentManager.createExtentReports();
        ExtentTest test = extent.createTest(scenario.getName() + " | Thread: " + Thread.currentThread().threadId());
        extentTest.set(test);
        test.log(Status.INFO, "Scenario Started");

        BaseTest.resetApplicationStateLogic();
        System.out.println("--- Hooks @Before Scenario: Setup Complete ---");
    }

    @After
    public void afterScenario(Scenario scenario) {
        System.out.println("--- Hooks @After Scenario: " + scenario.getName() + " ---");
        ExtentTest test = extentTest.get();
        if (test == null) {
             System.err.println("!!! ERROR in Hooks @After: ExtentTest is null for scenario: " + scenario.getName());
             return;
        }

        if (scenario.isFailed()) {
            test.log(Status.FAIL, "Scenario Failed. Status: " + scenario.getStatus());
            test.fail(scenario.getStatus().toString()); // Optionally add failure details
            try {
                if (BaseTest.driver != null && BaseTest.driver.getSessionId() != null) {
                    String screenshot = DriverMethods.getScreenshot();
                    if (screenshot != null && !screenshot.isEmpty()) {
                        test.addScreenCaptureFromBase64String(screenshot, "Failure Screenshot");
                    } else {
                        test.log(Status.WARNING, "Could not capture screenshot (empty result).");
                    }
                } else {
                     test.log(Status.WARNING, "Driver/Session not available, cannot take screenshot.");
                }
            } catch (Exception e) {
                System.err.println("!!! Error taking screenshot in Hooks @After: " + e.getMessage());
                test.log(Status.WARNING, "Exception occurred during screenshot capture: " + e.getMessage());
            }
        } else {
            test.log(Status.PASS, "Scenario Passed");
        }

        extentTest.remove();
        System.out.println("--- Hooks @After Scenario: Teardown Complete ---");
        BaseTest.quitDriverLogic();
    }

    @AfterAll
    public static void cleanupTestRun() {
        System.out.println("--- Hooks @AfterAll: Cleaning Up Test Run ---");
        BaseTest.stopServerLogic();
        if (extent != null) {
            System.out.println("--- Hooks @AfterAll: Flushing Extent Reports ---");
            extent.flush();
            System.out.println("--- Hooks @AfterAll: Reports Flushed ---");
        }
        System.out.println("--- Hooks @AfterAll: Cleanup Complete ---");
    }
}
