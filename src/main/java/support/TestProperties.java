package support;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties {
    private static Properties props = new Properties();
    // private static String propsPath = "src/main/resources/config.properties";
    private static String propsPath;
    private static String platform;
    private static DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
    
    static {
        loadProperties();
    }

    private static void loadProperties() {
        platform = System.getProperty("platform", "android").toLowerCase();
        System.out.println("Selected Platform: " + platform);

        propsPath = "src/main/resources/" + platform + ".config.properties";
        System.out.println("Loading properties from: " + propsPath);

        try (InputStream input = new FileInputStream(propsPath)) {
            props.load(input);
            System.out.println("Properties loaded successfully.");
            System.out.println(props);

            loadCapabilities();

        } catch (FileNotFoundException e) {
            System.err.println("!!! Configuration file not found: " + propsPath);
            System.err.println("!!! Make sure the file exists and the '-Dplatform' property (e.g., -Dplatform=android or -Dplatform=ios) is set correctly.");
            throw new RuntimeException("Configuration file not found: " + propsPath, e);
        } catch (IOException e) {
            System.err.println("!!! Error reading configuration file: " + propsPath);
            throw new RuntimeException("Error reading configuration file: " + propsPath, e);
        }
    }

    private static void loadCapabilities() {
        if (props.isEmpty()) {
            System.err.println("!!! Properties are empty. Cannot load capabilities.");
            return;
        }
        System.out.println("--- TestProperties: Loading capabilities for platform: " + platform);
        desiredCapabilities.setCapability("platformName", getProperty("platformName"));
        desiredCapabilities.setCapability("appium:automationName", getProperty("automationName"));
        // desiredCapabilities.setCapability("autoLaunch", getProperty("autoLaunch"));
        desiredCapabilities.setCapability("appium:fullReset", getProperty("fullReset"));
        desiredCapabilities.setCapability("appium:noReset", getProperty("noReset"));
        desiredCapabilities.setCapability("appium:autoWebView", getProperty("autoWebView"));
        desiredCapabilities.setCapability("appium:nativeWebScreenshot", getProperty("nativeWebScreenshot"));
        desiredCapabilities.setCapability("appium:newCommandTimeout", getProperty("newCommandTimeout"));

        if ("android".equals(platform)) {
            desiredCapabilities.setCapability("appium:appActivity", getProperty("appActivity"));
            desiredCapabilities.setCapability("appium:appPackage", getProperty("appPackage"));
            desiredCapabilities.setCapability("appium:nativeWebScreenshot", getProperty("nativeWebScreenshot"));
            desiredCapabilities.setCapability("appium:adbExecTimeout", getProperty("adbExecTimeout"));
        } else if ("ios".equals(platform)) {
            desiredCapabilities.setCapability("appium:deviceName", getProperty("deviceName"));
            desiredCapabilities.setCapability("appium:udid", getProperty("udid"));
            // desiredCapabilities.setCapability("appium:app", getProperty("app"));
            desiredCapabilities.setCapability("appium:bundleId", getProperty("bundleId"));
            desiredCapabilities.setCapability("appium:includeSafariInWebviews", getProperty("includeSafariInWebviews"));
            desiredCapabilities.setCapability("appium:fullContextList", getProperty("fullContextList"));
        }

        System.out.println("--- TestProperties: Desired Capabilities loaded: " + desiredCapabilities.toJson());
    }

    public static String getProperty(String name) {
        return props.getProperty(name);
    }

    public static DesiredCapabilities getLoadedCapabilities() {
        if (desiredCapabilities.getCapability("platformName") == null) {
             System.err.println("!!! Warning: Attempting to get capabilities before they are loaded or loading failed.");
             // loadProperties(); 
             // loadCapabilities();
        }
        return desiredCapabilities;
    }
}
