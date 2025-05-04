package support;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;

import org.testng.Reporter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DriverMethods {
    private static AppiumDriver driver;

    public static void setDriver(AppiumDriver driver) {
        DriverMethods.driver = driver;
    }
    
    public static void activateApp() {
        driver.executeScript("mobile: activateApp", Map.ofEntries(
                Map.entry("appId", TestProperties.getProperty("appPackage"))
        ));
    }

    public static void terminateApp() {
        driver.executeScript("mobile: terminateApp", Map.ofEntries(
                Map.entry("appId", TestProperties.getProperty("appPackage")),
                Map.entry("timeout", 1000)
        ));
    }

    public static String getScreenshot() {
        return driver.getScreenshotAs(OutputType.BASE64);
    }

    public static void logScreenShot() {
        String src = driver.getScreenshotAs(OutputType.BASE64);
        String path = "<img src=\"data:image/png;base64, " + src + "\" width=\"*\" height=\"350\"";
        Reporter.log(path);
    }

    public static void tapOnCoordinates(int x, int y) {
        driver.executeScript("mobile: clickGesture", Map.ofEntries(
                Map.entry("x", x),
                Map.entry("y", y)
        ));
    }

    public static void tapOnElem(WebElement element) {
        driver.executeScript("mobile: clickGesture", Map.ofEntries(
                Map.entry("elementId", ((RemoteWebElement) element).getId())));
    }

    public static void swipeByCoord(int left, int top, int width, int height,
                                    String direction, Double percent, int speed) {
        driver.executeScript("mobile: swipeGesture", Map.ofEntries(
                Map.entry("left", left),
                Map.entry("top", top),
                Map.entry("width", width),
                Map.entry("height", height),
                Map.entry("direction", direction),
                Map.entry("percent", percent),
                Map.entry("speed", speed)
        ));
    }

    public static void swipeByElem(WebElement element, String direction, Double percent, int speed) {
        driver.executeScript("mobile: swipeGesture", Map.ofEntries(
                Map.entry("elementId", ((RemoteWebElement) element).getId()),
                Map.entry("direction", direction),
                Map.entry("percent", percent),
                Map.entry("speed", speed)
        ));
    }

    public static void dragByCoord(int startX, int startY, int endX,
                                   int endY, int speed) {
        driver.executeScript("mobile: dragGesture", Map.ofEntries(
                Map.entry("startX", startX),
                Map.entry("startY", startY),
                Map.entry("endX", endX),
                Map.entry("endY", endY),
                Map.entry("speed", speed)
        ));
    }

    public static void dragElemToCoord(WebElement element, int endX,
                                       int endY, int speed) {
        driver.executeScript("mobile: dragGesture", Map.ofEntries(
                Map.entry("elementId", ((RemoteWebElement) element).getId()),
                Map.entry("endX", endX),
                Map.entry("endY", endY),
                Map.entry("speed", speed)
        ));
    }

    public static void tapOnCoordWithPointerInput(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(tap));
    }

    public static String getDeviceInfo () {
        String deviceManufacturer = (String) driver.executeScript("mobile: shell", Map.ofEntries(
                Map.entry("command", "getprop"),
//                Map.entry("args", "ro.product.brand"),
                Map.entry("args", "ro.product.manufacturer")
        ));

        String deviceModel = (String) driver.executeScript("mobile: shell", Map.ofEntries(
                Map.entry("command", "getprop"),
//                Map.entry("args", "ro.product.brand"),
                Map.entry("args", "ro.product.model")
        ));

        String deviceOSVersion = (String) driver.executeScript("mobile: shell", Map.ofEntries(
                Map.entry("command", "getprop"),
//                Map.entry("args", "ro.product.brand"),
                Map.entry("args", "ro.product.build.version.release")
        ));
        return "Device Manufacturer: " + deviceManufacturer + ", Model: " + deviceModel + ", OS Version: " + deviceOSVersion;
    }
}
