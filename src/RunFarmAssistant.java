 import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

 
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
     driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
   }
 
   @After
   public void quitDriver() {
     driver.quit();
   }
 
   @Test
	public void testGoogleSearch() throws InterruptedException, IOException {
		driver.get("http://www.tribalwars.net");
		
		waitForLoad(driver);

		WebElement loginName = driver.findElement(By.id("user"));
		loginName.sendKeys("SmallJohnson");
		
		WebElement loginPassword = driver.findElement(By.id("password"));
		loginPassword.sendKeys("33333");
		
	    List<WebElement> loginButton = driver.findElements(By.className("login_button"));
		loginButton.get(0).click();		
		System.out.println(loginButton.get(0).getAttribute("onclick"));
		waitForLoad(driver);
		
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("active_server")));   
		
		WebElement serverButton = driver.findElement(By.id("active_server"))
				.findElements(By.className("clearfix")).get(0)
				.findElements(By.tagName("a")).get(0);
		serverButton.click();
		
		waitForLoad(driver);
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("manager_icon")));
		
		WebElement goToFarmAssistant = driver.findElements(By.className("manager_icon")).get(0);
		goToFarmAssistant.click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));   
		
		int lightCavToSend = Integer.parseInt(driver.findElements(By.name("light")).get(1).getAttribute("value"));
		int lightCavRemaining = Integer.parseInt(driver.findElement(By.id("light")).getAttribute("textContent"));
		
		System.out.println("toSend = " + lightCavToSend + " Remaining = " + lightCavRemaining);
		
		List<WebElement> farmButtons = driver.findElements(By.className("farm_icon_b"));
		for(int i = 1; i < farmButtons.size(); i++){
			//farmButtons.get(i).click();
		}
			
		
		Thread.sleep(100000);
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
 