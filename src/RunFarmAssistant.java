 import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;




import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.thoughtworks.selenium.Wait;
 
 @RunWith(JUnit4.class)
 public class RunFarmAssistant extends TestCase {
 
   private static ChromeDriverService service;
   private WebDriver driver;
 
   @BeforeClass
   public static void createAndStartService() throws IOException {
	 String chromePath = "C:\\Users\\Arwin\\TribalWarsSelenium\\TribalWarsSelenium\\chromedriver_win32\\chromedriver.exe"; 
	 service = new ChromeDriverService.Builder()
         .usingDriverExecutable(new File(chromePath))
         .usingAnyFreePort()
         .build();
     service.start();
     
     
   }
 
   @AfterClass
   public static void createAndStopService() {
     service.stop();
   }
 
   @Before
   public void createDriver() {
     driver = new RemoteWebDriver(service.getUrl(),
         DesiredCapabilities.chrome());
   }
 
   @After
   public void quitDriver() {
     driver.quit();
   }
 
   @Test
	public void testGoogleSearch() {
		driver.get("http://www.tribalwars.net");
		waitForLoad(driver);
		/*
		 * WebElement searchBox = driver.findElement(By.id("user"));
		 * searchBox.sendKeys("SmallJohnson"); WebElement searchbutton =
		 * driver.findElement(By.id("gbqfba")); searchbutton.click();
		 */
		/*JavascriptExecutor js;
		js = (JavascriptExecutor) driver;
		js.executeScript("document.getElementById('user').value = 'SmallJohnson';");*/
	}
   
   void waitForLoad(WebDriver driver) {
	    ExpectedCondition<Boolean> pageLoadCondition = new
	        ExpectedCondition<Boolean>() {
	            public Boolean apply(WebDriver driver) {
	                return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
	            }
	        };
	    WebDriverWait wait = new WebDriverWait(driver, 30);
	    wait.until(pageLoadCondition);
	}
}
 