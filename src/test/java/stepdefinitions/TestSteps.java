package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.testng.Assert;
import org.testng.annotations.Listeners;

import base.BaseTest;

@Listeners
public class TestSteps extends BaseTest {

   @Given("I am on welcome screen")
    public void i_am_on_welcome_screen() {
        Assert.assertTrue(welcomePage.welcomePageLoaded(), "Welcome page isn't loaded");
    }
     
    @When("I tap on skip button")
    public void i_tap_on_skip_button() {
        welcomePage.clickSkipButton();    
    }

    @Then("Schedule screen is opened")
    public void schedule_screen_is_opened() {
        Assert.assertTrue(schedulePage.schedulePageLoaded(), "Schedule page isn't loaded");   
    }
}
