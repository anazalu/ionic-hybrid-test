package screens;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WelcomePage {

    protected AppiumDriver driver;
    private WebDriverWait wait;
    private final By skipButtonBy = By.xpath("//ion-button[@id=\"skip_tutorial_btn\"]");
    // private final By welcomeTextBy = By.xpath("//ion-slide[contains(@class, 'swiper-slide-active') and .//h2[text()=' Welcome to ']]");
    

    public WelcomePage(AppiumDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
        // wait = new WebDriverWait(this.driver, Duration.ofSeconds(5));
    }

    public boolean welcomePageLoaded() {
        WebElement skipBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(skipButtonBy));
        return skipBtn.isDisplayed();
    }

    public void clickSkipButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(skipButtonBy)).click();    }
}
