package support;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;
import java.util.Set;

public class ContextSwitcher {

    private static AppiumDriver driver;
    private static WebDriverWait wait;

    public ContextSwitcher(AppiumDriver driver, WebDriverWait wait) {
        ContextSwitcher.driver = driver;
        ContextSwitcher.wait = wait;
    }

    private static String extractContextId(Object ctx) {
        if (ctx instanceof String) {
            return (String) ctx;
        } else if (ctx instanceof java.util.Map) {
           Object id = ((Map<?, ?>) ctx).get("id");
            if (id instanceof String) {
                return (String) id;
            }
        }
        return null;
    }

    public static void switchToWebView() {
        System.out.println("--- ContextSwitcher: Attempting to switch to WebView context...");

        try {
            ExpectedCondition<Boolean> webViewAvailable = wd -> {
                Set<?> contexts = ((SupportsContextSwitching) driver).getContextHandles();
                System.out.println("--- ContextSwitcher: Checking contexts: " + contexts); 
                for (Object ctx : contexts) {
                    String context = extractContextId(ctx);
                    System.out.println("--- ContextSwitcher: loop through contexts: " + context); 
                    
                    if (context.toLowerCase().contains("webview")) {
                        return true; 
                    }
                }
                return false; 
            };

            System.out.println("--- ContextSwitcher: Waiting for WebView context to become available...");
            wait.until(webViewAvailable);

            Set<?> rawContexts = ((SupportsContextSwitching) driver).getContextHandles();
            boolean switched = false;
            for (Object ctx : rawContexts) {
                String context = extractContextId(ctx);
                if (context.toLowerCase().contains("webview")) {
                    ((SupportsContextSwitching) driver).context(context);
                    System.out.println("--- ContextSwitcher: Successfully switched to WebView: " + context);
                    switched = true;
                    break; 
                }
            }

            if (!switched) {
                System.err.println("!!! Error: Waited for WebView context, but failed to switch afterwards.");
                printAllContexts(); 
                throw new RuntimeException("No WebView context found to switch to, even after waiting.");
            }

        } catch (TimeoutException e) {
            System.err.println("!!! Error: Timed out waiting for WebView context after " + wait.toString() + " seconds.");
            printAllContexts(); 
            throw new RuntimeException("No WebView context found within the timeout period.", e);
        } catch (Exception e) {
            System.err.println("!!! An unexpected error occurred during WebView context switch: " + e.getMessage());
            e.printStackTrace(); 
            printAllContexts();
            throw e; 
        }
    }

    public static void switchToNative() {
         if (driver == null) {
            System.err.println("!!! Driver is null in switchToNative. Cannot switch context.");
            return; 
        }
        System.out.println("--- ContextSwitcher: Switching to Native App context...");
        try {
             ((SupportsContextSwitching) driver).context("NATIVE_APP");
             System.out.println("--- ContextSwitcher: Successfully switched to Native App");
        } catch (Exception e) {
            System.err.println("!!! Error switching to Native App context: " + e.getMessage());
            printAllContexts();
        }
    }

    public static String getCurrentContext() {
        if (driver == null) return "Driver is null";
        try {
            return ((SupportsContextSwitching) driver).getContext();
        } catch (Exception e) {
            return "Error getting context: " + e.getMessage();
        }
    }

    private static void printAllContexts() {
        if (driver == null) {
            System.out.println("--- ContextSwitcher: Available contexts: Driver is null.");
            return;
        }
        try {
            System.out.println("--- ContextSwitcher: Available contexts:");
            for (Object ctx : ((SupportsContextSwitching) driver).getContextHandles()) {
                String context = extractContextId(ctx);
                System.out.println("- " + context);
            }
        } catch (Exception e) {
            System.out.println("Error retrieving contexts: " + e.getMessage());
        }
    }
}
