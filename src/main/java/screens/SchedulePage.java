package screens;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SchedulePage {

    protected AppiumDriver driver;
    private WebDriverWait wait;

    private final By favoritesButtonBy = By.xpath("//ion-segment-button[@value=\"favorites\"]");

    public SchedulePage(AppiumDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
        // wait = new WebDriverWait(this.driver, Duration.ofSeconds(5));
    }

    public boolean schedulePageLoaded() {
        WebElement favoritesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(favoritesButtonBy));
        return favoritesButton.isDisplayed();
    }
}
