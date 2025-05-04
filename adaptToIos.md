Okay, let's adapt your project to run tests on either Android or iOS based on a parameter. We'll use Maven profiles and system properties to manage the platform selection and configurations.

**Key Changes:**

1.  **Configuration Files:** Create separate properties files for Android and iOS capabilities.
2.  **`TestProperties.java`:** Modify it to load the correct properties file based on a system property (`platform`).
3.  **`BaseTest.java`:** Refactor to remove TestNG annotations and provide static logic methods for setup/teardown, which will be called by Cucumber Hooks. The driver instantiation will use the loaded capabilities.
4.  **`Hooks.java`:** Implement Cucumber's `@BeforeAll`, `@AfterAll`, `@Before`, `@After` hooks to manage the test lifecycle (server start/stop, driver setup/teardown per scenario).
5.  **`RunCucumberTest.java`:** Remove any base class extension if it existed.
6.  **`pom.xml`:** Add the Maven Surefire plugin configuration to accept the system property.
7.  **Page Objects:** (Minor) Update constructors to accept `WebDriverWait`. Potentially use `@AndroidFindBy`/`@iOSXCUITFindBy` for platform-specific locators if needed later.
8.  **`ContextSwitcher.java`:** Make `switchToWebView` more robust with explicit waits.

**Step-by-Step Implementation:**

**1. Create Platform-Specific Configuration Files:**

*   Rename `src/main/resources/config.properties` to `src/main/resources/android.config.properties`.
*   Create a new file `src/main/resources/ios.config.properties` with your iOS capabilities:

    ```properties
    # src/main/resources/ios.config.properties
    platformName=iOS
    appium:automationName=XCUITest
    appium:deviceName=iPhone 12 16.1   # Adjust as needed
    appium:udid=575D2F83-C4BA-4236-A927-7F5B224B5C54 # Adjust as needed
    appium:app=com.ionicframework.conferenceapp # Or path to .app/.ipa if not installed
    appium:includeSafariInWebviews=true
    appium:autoWebview=true
    appium:fullContextList=true
    appium:newCommandTimeout=1200
    # appium:platformVersion=16.1 # Often good to include explicitly
    appium:wdaLaunchTimeout=240000 # Increase WDA timeout for iOS
    appium:wdaConnectionTimeout=240000
    # Add bundleId if using app path instead of bundleId directly in 'app'
    # appium:bundleId=com.ionicframework.conferenceapp
    appium:noReset=false # Or true, depending on your needs
    appium:fullReset=false # Mutually exclusive with noReset typically
    ```

*   Update `src/main/resources/android.config.properties` to use `appium:` prefix for consistency and ensure `appPackage`/`appActivity` are correct:

    ```properties
    # src/main/resources/android.config.properties
    platformName=Android
    appium:automationName=UiAutomator2
    # appium:app=/path/to/your/conference-app-android.apk # Uncomment and set path if needed
    appium:appActivity=com.ionicframework.conferenceapp.MainActivity
    appium:appPackage=com.ionicframework.conferenceapp
    # appium:autoLaunch=true # Often default, can be omitted
    appium:fullReset=false
    appium:noReset=false # Set one of these to true if desired, usually not both false
    appium:autoWebView=true
    appium:nativeWebScreenshot=true
    appium:newCommandTimeout=1200
    appium:adbExecTimeout=60000 # Added from your BaseTest example
    # appium:deviceName=YourAndroidDeviceName # Optional but recommended
    # appium:udid=YourAndroidDeviceUDID # Optional but recommended if multiple devices
    # appium:platformVersion=13 # Optional but recommended
    ```

**2. Modify `TestProperties.java`:**

```java
package support;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties {
    private static final Properties props = new Properties();
    private static String platform; // Store the determined platform
    private static String propsPath; // Store the determined path
    private static final DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

    // Load properties only once
    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Determine platform from system property, default to 'android' if not provided
        platform = System.getProperty("platform", "android").toLowerCase();
        System.out.println("Selected Platform: " + platform); // Log selected platform

        propsPath = "src/main/resources/" + platform + ".config.properties";
        System.out.println("Loading properties from: " + propsPath);

        try (InputStream input = new FileInputStream(propsPath)) {
            props.load(input);
            System.out.println("Properties loaded successfully.");
            // Load capabilities *after* properties are loaded
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

    // New method to populate capabilities based on loaded properties
    private static void loadCapabilities() {
        if (props.isEmpty()) {
             System.err.println("!!! Properties are empty. Cannot load capabilities.");
             return; // Or throw error
        }
        System.out.println("Loading capabilities for platform: " + platform);
        // Common capabilities
        desiredCapabilities.setCapability("platformName", getProperty("platformName")); // Mandatory
        setPropertyIfPresent("appium:automationName");
        setPropertyIfPresent("appium:app"); // Path or bundleId/package
        setPropertyIfPresent("appium:deviceName");
        setPropertyIfPresent("appium:udid");
        setPropertyIfPresent("appium:platformVersion");
        setPropertyIfPresent("appium:autoWebView");
        setPropertyIfPresent("appium:newCommandTimeout");
        setPropertyIfPresent("appium:noReset");
        setPropertyIfPresent("appium:fullReset");

        // Platform-specific capabilities (use appium prefix where applicable)
        if ("android".equals(platform)) {
            setPropertyIfPresent("appium:appPackage");
            setPropertyIfPresent("appium:appActivity");
            setPropertyIfPresent("appium:nativeWebScreenshot");
            setPropertyIfPresent("appium:adbExecTimeout");
            // Add other Android-specific caps here if needed
        } else if ("ios".equals(platform)) {
            setPropertyIfPresent("appium:includeSafariInWebviews");
            setPropertyIfPresent("appium:fullContextList");
            setPropertyIfPresent("appium:wdaLaunchTimeout");
            setPropertyIfPresent("appium:wdaConnectionTimeout");
            setPropertyIfPresent("appium:bundleId"); // Can be alternative to 'app'
            // Add other iOS-specific caps here if needed
        }

        System.out.println("Desired Capabilities loaded: " + desiredCapabilities.toJson());
    }

    // Helper to avoid setting null capabilities
    private static void setPropertyIfPresent(String key) {
        String value = getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            // Handle boolean conversion explicitly for relevant caps
            if (key.equals("appium:autoWebView") || key.equals("appium:noReset") ||
                key.equals("appium:fullReset") || key.equals("appium:nativeWebScreenshot") ||
                key.equals("appium:includeSafariInWebviews") || key.equals("appium:fullContextList")) {
                 desiredCapabilities.setCapability(key, Boolean.parseBoolean(value));
            }
            // Handle integer conversion explicitly for relevant caps
            else if (key.equals("appium:newCommandTimeout") || key.equals("appium:adbExecTimeout") ||
                     key.equals("appium:wdaLaunchTimeout") || key.equals("appium:wdaConnectionTimeout")) {
                try {
                    desiredCapabilities.setCapability(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                     System.err.println("!!! Warning: Could not parse integer value for capability '" + key + "': " + value);
                     desiredCapabilities.setCapability(key, value); // Keep as string if parsing fails? Or throw error?
                }
            }
            else {
                desiredCapabilities.setCapability(key, value);
            }
        } else {
             System.out.println("Property '" + key + "' not found or empty in " + propsPath + ", skipping capability.");
        }
    }


    public static String getProperty(String name) {
        return props.getProperty(name);
    }

    // Renamed for clarity - capabilities are now loaded automatically
    public static DesiredCapabilities getLoadedCapabilities() {
        if (desiredCapabilities.getCapability("platformName") == null) {
             // This indicates properties/capabilities might not have loaded correctly.
             System.err.println("!!! Warning: Attempting to get capabilities before they are loaded or loading failed.");
             // Optionally try loading again, but better to ensure static block runs first.
             // loadProperties(); // Be careful about re-entrancy
             // loadCapabilities();
        }
        return desiredCapabilities;
    }

     // Add a getter for the platform if needed elsewhere
     public static String getPlatformName() {
         if (platform == null) {
              loadProperties(); // Ensure properties (and platform) are loaded
         }
         return platform;
     }
}
```

**3. Refactor `BaseTest.java`:**

*(Use the updated `BaseTest.java` provided in the prompt, as it already reflects the separation of logic into static methods and removal of TestNG annotations. Ensure it uses `TestProperties.getLoadedCapabilities()`)*

*   **Important Change:** In `setupDriverLogic`, use the capabilities loaded by `TestProperties`:

    ```java
        // Inside setupDriverLogic() method in BaseTest.java
        public static void setupDriverLogic() throws MalformedURLException {
            System.out.println("--- BaseTest Logic: Setting up driver ---");
            if (driver == null) {
                // Get the capabilities already loaded by TestProperties
                DesiredCapabilities desiredCapabilities = TestProperties.getLoadedCapabilities(); // Use the loaded caps

                if (desiredCapabilities == null || desiredCapabilities.getCapability("platformName") == null) {
                    throw new RuntimeException("DesiredCapabilities or platformName is null. Ensure properties were loaded correctly via TestProperties.");
                }

                // No need to set adbExecTimeout here if it's in the properties file
                // desiredCapabilities.setCapability("appium:adbExecTimeout", 60000); // Remove if handled by properties

                // Get platform from the capabilities themselves
                platform = desiredCapabilities.getCapability("platformName").toString().toLowerCase();
                URL serverUrl = server.getUrl(); // Assumes server is already started

                // ... rest of the driver instantiation logic remains the same ...
                 try {
                    System.out.println("--- BaseTest Logic: Creating driver for platform: " + platform + " at URL: " + serverUrl);
                    System.out.println("--- BaseTest Logic: Using Capabilities: " + desiredCapabilities.toJson()); // Log caps
                    if (platform.equals("android")) {
                        driver = new AndroidDriver(serverUrl, desiredCapabilities);
                    } else if (platform.equals("ios")) {
                        driver = new IOSDriver(serverUrl, desiredCapabilities);
                    } else {
                        throw new IllegalArgumentException("Unsupported platform: " + platform);
                    }
                    System.out.println("--- BaseTest Logic: Driver Session Created: " + driver.getSessionId());
                } catch (Exception e) {
                     System.err.println("!!! Failed to create Appium Driver session !!!");
                     System.err.println("Capabilities used: " + desiredCapabilities.toJson());
                     e.printStackTrace();
                     // Consider stopping server if driver fails definitively
                     if (server != null && server.isRunning()) {
                          System.err.println("Attempting to stop server due to driver creation failure.");
                          stopServerLogic();
                     }
                     throw new RuntimeException("Failed to create Appium driver session", e);
                }

                // ... initialization of wait, pages, etc. ...
            } else {
                System.out.println("--- BaseTest Logic: Driver already initialized.");
            }
        }

        // In resetApplicationStateLogic, use getProperty for platform-specific ID
        public static void resetApplicationStateLogic() {
            // ... (previous logs)
            try {
                // Determine app identifier based on platform
                String appIdentifierKey = "android".equals(platform) ? "appium:appPackage" : "appium:bundleId";
                // Fallback for iOS if bundleId isn't set but 'app' is the bundleId
                 if ("ios".equals(platform) && TestProperties.getProperty(appIdentifierKey) == null) {
                     appIdentifierKey = "appium:app"; // Assume 'app' holds the bundleId for iOS if bundleId is missing
                 }

                String appIdentifier = TestProperties.getProperty(appIdentifierKey);

                if (appIdentifier == null || appIdentifier.trim().isEmpty()) {
                    System.err.println("!!! Warning: App identifier property ('" + appIdentifierKey + "') is missing or empty in " + propsPath + ". Cannot terminate/activate app by identifier.");
                    // Decide action: Skip terminate/activate or throw error? Skipping for now.
                } else {
                    // 1. Terminate
                    System.out.println("--- BaseTest Logic: Terminating app: " + appIdentifier);
                    // Use the generic executeScript for terminate/activate
                     driver.executeScript("mobile: terminateApp", Map.of("appId", appIdentifier));
                    // Optional short pause if needed
                    // Thread.sleep(500);

                    // 2. Activate
                    System.out.println("--- BaseTest Logic: Activating app: " + appIdentifier);
                     driver.executeScript("mobile: activateApp", Map.of("appId", appIdentifier));
                     // Optional short pause
                     // Thread.sleep(500);
                }

                // 3. Switch to WebView
                System.out.println("--- BaseTest Logic: Switching to WebView...");
                ContextSwitcher.switchToWebView(); // Waits internally

            } catch (Exception e) {
                System.err.println("Warning: Exception during application state reset (terminate/activate/switch context).");
                e.printStackTrace();
                // Consider re-throwing depending on test needs
            }
            System.out.println("--- BaseTest Logic: Application state reset finished.");
        }
    ```

**4. Implement `Hooks.java`:**

*(Use the updated `Hooks.java` provided in the prompt. It correctly uses `@BeforeAll`, `@AfterAll`, `@Before`, `@After` and calls the static logic methods from `BaseTest`.)*

**5. Update `RunCucumberTest.java`:**

*(The version in the prompt is already correct - it just extends `AbstractTestNGCucumberTests` and has the `@CucumberOptions`.)* Ensure it doesn't extend `BaseTest`.

**6. Configure `pom.xml`:**

Add the Maven Surefire Plugin to handle the system property and run TestNG (which runs Cucumber).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>tempIonic</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source> <!-- Changed to 11 for wider compatibility, use 20 if needed -->
        <maven.compiler.target>11</maven.compiler.target> <!-- Changed to 11 for wider compatibility, use 20 if needed -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <testng.suite.xml>all-tests.xml</testng.suite.xml> <!-- Default suite file -->
        <platform>android</platform> <!-- Default platform if none specified -->
    </properties>

    <dependencies>
        <!-- Appium Java Client -->
        <dependency>
            <groupId>io.appium</groupId>
            <artifactId>java-client</artifactId>
            <version>9.4.0</version>
        </dependency>
        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.11.0</version>
            <!-- Scope should ideally be test, but compile works if needed elsewhere -->
            <scope>test</scope>
            <!-- <scope>compile</scope> -->
        </dependency>
        <!-- Extent Reports -->
        <dependency>
            <groupId>com.aventstack</groupId>
            <artifactId>extentreports</artifactId>
            <version>5.1.2</version>
        </dependency>
        <!-- Cucumber Java -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>7.20.1</version>
             <scope>test</scope>
        </dependency>
        <!-- Cucumber TestNG -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>7.20.1</version>
             <scope>test</scope>
        </dependency>
         <!-- SLF4J API (often required by Appium/Selenium internals, good practice) -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.13</version> <!-- Use a recent version -->
            <scope>test</scope>
        </dependency>
        <!-- SLF4J Simple Logger (provides a basic console logger) -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.13</version>
            <scope>test</scope>
        </dependency>

        <!-- Ensure Selenium dependencies are compatible if not brought in by java-client -->
        <!-- Usually java-client manages this, but check if errors occur -->
        <!--
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.xx.x</version> <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-support</artifactId>
            <version>4.xx.x</version> <scope>test</scope>
        </dependency>
         -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version> <!-- Use a recent version -->
                <configuration>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.4.0</version> <!-- Use a recent version -->
                <configuration>
                    <suiteXmlFiles>
                        <!-- Make sure this path is correct -->
                        <suiteXmlFile>src/test/resources/${testng.suite.xml}</suiteXmlFile>
                    </suiteXmlFiles>
                    <systemPropertyVariables>
                        <!-- Pass the platform property to the tests -->
                        <platform>${platform}</platform>
                        <!-- You can add other system properties here if needed -->
                    </systemPropertyVariables>
                     <!-- Recommended: Run each test class in its own JVM fork for isolation -->
                     <forkCount>1</forkCount>
                     <reuseForks>false</reuseForks>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```
*   **Move `all-tests.xml`:** Move your `all-tests.xml` file to `src/test/resources/all-tests.xml` so Surefire can find it easily with the configuration above.

**7. Update Page Object Constructors:**

Ensure your page object constructors accept the `WebDriverWait` instance passed from `BaseTest`. Your current `SchedulePage` and `WelcomePage` already seem to do this, which is correct.

```java
// Example: src/main/java/screens/WelcomePage.java (constructor)
public WelcomePage(AppiumDriver driver, WebDriverWait wait) {
    this.driver = driver;
    this.wait = wait; // Use the passed-in wait
    // Initialize elements using AppiumFieldDecorator
    PageFactory.initElements(new AppiumFieldDecorator(driver), this);
}

// Example: src/main/java/screens/SchedulePage.java (constructor)
public SchedulePage(AppiumDriver driver, WebDriverWait wait) {
    this.driver = driver;
    this.wait = wait; // Use the passed-in wait
    // Initialize elements using AppiumFieldDecorator
    PageFactory.initElements(new AppiumFieldDecorator(driver), this);
}
```

**8. Update `ContextSwitcher.java`:**

*(Use the updated `ContextSwitcher.java` provided in the prompt. It includes `WebDriverWait` for robustness.)*

**How to Run:**

1.  **Ensure Appium Server is NOT running manually.** The code will start/stop it. Make sure port 4724 (or the one configured) is free.
2.  **Open your terminal/command prompt.**
3.  **Navigate to your project's root directory** (where `pom.xml` is).
4.  **Run for Android (default or explicit):**
    *   `mvn clean test`
    *   `mvn clean test -Dplatform=android`
5.  **Run for iOS:**
    *   `mvn clean test -Dplatform=ios`

Maven will compile the code, read the `-Dplatform` property, pass it to the Surefire plugin, which makes it available via `System.getProperty("platform")` in your `TestProperties` class. `TestProperties` will load the correct `*.config.properties` file, and the tests will execute against the specified platform. The `Hooks` class will manage the driver lifecycle for each scenario.