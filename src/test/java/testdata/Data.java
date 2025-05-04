package testdata;

import dataObjects.Credentials;
import org.testng.annotations.DataProvider;

public class Data {
    public static String[] expectedTitles = {"FULLY OPEN SOURCE", "GREAT COMMUNITY", "JS.FOUNDATION", "SUPPORT VIDEOS", "EXTENDABLE", "COMPATIBLE"};

    @DataProvider(name = "valid-login-signup")
    public static Object[][] getValidCredentials() {
        return new Object[][] {
                {new Credentials("email@email.com", "password")}
        };
    }

    @DataProvider(name = "invalid-login")
    public static Object[][] getInValidCredentials () {
        return new Object[][] {
                {new Credentials("notanemail", "password", "Please enter a valid email address")},
                {new Credentials("emailexample.com", "password", "Please enter a valid email address")},
                {new Credentials("email@examplecom", "password", "Please enter a valid email address")},
                {new Credentials("email@email.com", "p", "Please enter at least 8 characters")}
        };
    }

    @DataProvider(name = "invalid-signup")
    public static Object[][] getValidSignUp() {
        return new Object[][] {
                {new Credentials(
                        "email@email.com",
                        "password",
                        "anotherpassword",
                        "Please enter the same password")}
                ,
                {new Credentials(
                        "emailexample.com",
                        "password",
                        "password",
                        "Please enter a valid email address")}
                ,
                {new Credentials(
                        "email@email.com",
                        "p",
                        "p",
                        "Please enter at least 8 characters")}
                ,
                {new Credentials(
                        "notanemail",
                        "p",
                        "pas",
                        "Please enter at least 8 characters")}
        };
    }

    @DataProvider(name = "dropdown-option")
    public static Object[][] getDropdownOptions() {
        String defaultDropdownOption = "Select an item...";
        return new Object[][] {
                {defaultDropdownOption, "webdriver.io is awesome"},
                {defaultDropdownOption, "Appium is awesome"},
                {defaultDropdownOption, "This app is awesome"}
        };
    }
}
