package base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import screens.SchedulePage;
import screens.WelcomePage;
import support.ContextSwitcher;
import support.DriverMethods;
import support.TestProperties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

public class BaseTest {

    public static AppiumDriverLocalService server;
    public static AppiumDriver driver;
    public static WebDriverWait wait;
    public static ContextSwitcher contextSwitcher;
    public static WelcomePage welcomePage;
    public static SchedulePage schedulePage;
    public static String platform;

    public static void loadPropertiesLogic() {
        System.out.println("--- BaseTest Logic: Loading properties...");
        // TestProperties.loadProperties();
        System.out.println("--- BaseTest Logic: Properties loaded.");
    }

    public static void startServerLogic() {
        System.out.println("--- BaseTest Logic: Starting Appium Server...");
        AppiumServiceBuilder appiumServiceBuilder = new AppiumServiceBuilder();
        appiumServiceBuilder.usingPort(4724);
        appiumServiceBuilder.withLogFile(new File("appium-server-logs.log"));
        appiumServiceBuilder.withArgument(GeneralServerFlag.RELAXED_SECURITY);
        // server = AppiumServiceBuilder.buildDefaultService();
        server = appiumServiceBuilder.build();
        server.clearOutPutStreams();
        try {
            server.start();
            System.out.println("--- BaseTest Logic: Appium Server Started at: " + server.getUrl());
        } catch (Exception e) {
            System.err.println("!!! Appium Server failed to start !!! Check if port 4724 is free.");
            e.printStackTrace();
            throw new RuntimeException("Appium server failed to start", e);
        }
    }

    public static void setupDriverLogic() throws MalformedURLException {
        System.out.println("--- BaseTest Logic: Setting up driver ---");
        if (driver == null) {
            DesiredCapabilities desiredCapabilities = TestProperties.getLoadedCapabilities();
            if (desiredCapabilities == null || desiredCapabilities.getCapability("platformName") == null) {
                throw new RuntimeException("DesiredCapabilities or platformName is null. Ensure properties were loaded.");
            }

            platform = desiredCapabilities.getCapability("platformName").toString().toLowerCase();
            URL serverUrl = server.getUrl();

            try {
                System.out.println("--- BaseTest Logic: Creating driver for platform: " + platform + " at URL: " + serverUrl);
                if (platform.equals("android")) {
                    System.out.println("--- BaseTest Logic: Desired Capabilities loaded: " + desiredCapabilities.toJson());
                    driver = new AndroidDriver(serverUrl, desiredCapabilities);
                } else if (platform.equals("ios")) {
                    driver = new IOSDriver(serverUrl, desiredCapabilities);
                } else {
                    throw new IllegalArgumentException("Unsupported platform: " + platform);
                }
                System.out.println("--- BaseTest Logic: Driver Session Created: " + driver.getSessionId());
            } catch (Exception e) {
                System.err.println("!!! Failed to create Appium Driver session !!!");
                e.printStackTrace();
                if (server != null && server.isRunning()) { stopServerLogic(); }
                throw new RuntimeException("Failed to create Appium driver session", e);
            }

            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            DriverMethods.setDriver(driver);
            
            contextSwitcher = new ContextSwitcher(driver, wait);
            welcomePage = new WelcomePage(driver, wait);
            schedulePage = new SchedulePage(driver, wait);
            System.out.println("--- BaseTest Logic: Driver setup complete.");
        } else {
            System.out.println("--- BaseTest Logic: Driver already initialized.");
        }
    }

    public static void resetApplicationStateLogic() {
        try {
            String appIdentifierKey = "android".equals(platform) ? "appPackage" : "bundleId";
            String appCommandParam = "android".equals(platform) ? "appId" : "bundleId";

            String appIdentifier = TestProperties.getProperty(appIdentifierKey);
            System.out.println("--- BaseTest Logic: appIdentifierKey: " + appIdentifierKey);
            System.out.println("--- BaseTest Logic: appIdentifier: " + appIdentifier);

            if (appIdentifier == null || appIdentifier.trim().isEmpty()) {
                System.err.println("!!! Warning: App identifier property ('" + appIdentifierKey + "') is missing or empty. Cannot terminate/activate app by identifier.");
            } else {
                System.out.println("--- BaseTest Logic: Terminating app: " + appIdentifier);
                driver.executeScript("mobile: terminateApp", Map.of(appCommandParam, appIdentifier));

                System.out.println("--- BaseTest Logic: Activating app: " + appIdentifier);
                driver.executeScript("mobile: activateApp", Map.of(appCommandParam, appIdentifier));
            }

            System.out.println("--- BaseTest Logic: Switching to WebView...");
            ContextSwitcher.switchToWebView();
            System.out.println("--- BaseTest Logic: Switched to WebView!");

        } catch (Exception e) {
            System.err.println("Warning: Exception during application state reset (terminate/activate/switch context).");
            e.printStackTrace();
        }
        System.out.println("--- BaseTest Logic: Application state reset finished.");
    }

    public static void quitDriverLogic() {
        System.out.println("--- BaseTest Logic: Tearing down driver ---");
        if (driver != null) {
            System.out.println("--- BaseTest Logic: Quitting Appium Driver...");
            try {
                driver.quit();
                System.out.println("--- BaseTest Logic: Driver Quitted.");
            } catch (Exception e) {
                System.err.println("!!! Error quitting driver: " + e.getMessage());
            } finally {
                driver = null;
                wait = null;
                contextSwitcher = null;
                welcomePage = null;
                schedulePage = null;
            }
        } else {
            System.out.println("--- BaseTest Logic: Driver was already null.");
        }
    }

    public static void stopServerLogic() {
        System.out.println("--- BaseTest Logic: Shutting down server ---");
        if (server != null && server.isRunning()) {
            System.out.println("--- BaseTest Logic: Stopping Appium Server...");
            server.stop();
            System.out.println("--- BaseTest Logic: Appium Server Stopped.");
        } else {
            System.out.println("--- BaseTest Logic: Server was null or not running.");
        }
    }
}
