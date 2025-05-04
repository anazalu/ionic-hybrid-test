package reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class ExtentTestManager {

    public static ExtentReports extent = ExtentManager.createExtentReports();
    public static ExtentTest test;

    public static ExtentTest getTest() {
        return test;
    }

    public static void startTest(String testName, String desc) {
        test = extent.createTest(testName, desc);
    }
}
