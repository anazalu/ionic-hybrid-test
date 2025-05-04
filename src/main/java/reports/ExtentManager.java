package reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class ExtentManager {
    public static final ExtentReports extentReports = new ExtentReports();
    public static String reportPath = String.format("./extent-reports/extent-report-%s.html",
            new SimpleDateFormat("yyyyMMdd-HHmmss").format(Date.from(Instant.now())));

    public static ExtentReports createExtentReports() {
        ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
        reporter.config().setReportName("Extent Report - Amtrac app");
        extentReports.attachReporter(reporter);
        extentReports.setSystemInfo("OS", String.format("%s %s", System.getProperty("os.name"), System.getProperty("os.version")));
        extentReports.setSystemInfo("Author", "Zalunina");

        return extentReports;
    }
}
