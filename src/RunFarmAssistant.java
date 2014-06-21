import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@RunWith(JUnit4.class)
public class RunFarmAssistant extends TestCase {

	private static final String WINDOWS32_CHROMEPATH = "chromedriver_win32\\chromedriver.exe";
	private static final String LINUX64_CHROMEPATH   = "chromedriver_linux64/chromedriver";
	private static final String LINUX32_CHROMEPATH   = "chromedriver_linux32/chromedriver";
	private static final String USERNAME 			 = "SmallJohnson";
	private static final String PASSWORD			 = "33333";
	private static final int FARMINTERVAL_MILS		 = 10000000;
	
	private static ChromeDriverService service;
	private WebDriver driver;
	
	@BeforeClass
	public static void createAndStartService() throws IOException {
		service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File(LINUX32_CHROMEPATH)).usingAnyFreePort()
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
	public void farm() throws InterruptedException, IOException {
		driver.get("http://www.tribalwars.net");

		//waitForLoad(driver);

		WebElement loginName = driver.findElement(By.id("user"));
		loginName.sendKeys(USERNAME);

		WebElement loginPassword = driver.findElement(By.id("password"));
		loginPassword.sendKeys(PASSWORD);

		List<WebElement> loginButton = driver.findElements(By.className("login_button"));
		loginButton.get(0).click();

		//waitForLoad(driver);

		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("active_server")));

		WebElement serverButton = driver.findElement(By.id("active_server"))
				.findElements(By.className("clearfix")).get(0)
				.findElements(By.tagName("a")).get(0);
		serverButton.click();

		//waitForLoad(driver);

		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("manager_icon")));

		WebElement goToFarmAssistant = driver.findElements(By.className("manager_icon")).get(0);
		goToFarmAssistant.click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));

		int firstPageNum = Integer.parseInt(driver.findElements(By.tagName("Strong")).get(0).getAttribute("textContent").substring(2,3)) - 1;
		int numPages = driver.findElements(By.className("paged-nav-item")).size();
		boolean select;
		try{ 
			select = driver.findElement(By.id("select")) != null;
		}
		catch (NoSuchElementException e){
			select = false;
		}
		
		for(int pageNum = firstPageNum; pageNum < numPages; pageNum++){
			List<WebElement> pageNavItems = driver.findElements(By.className("paged-nav-item"));
			
			int lightCavToSend = Integer.parseInt(driver.findElements(By.name("light")).get(1).getAttribute("value"));
			int lightCavRemaining = Integer.parseInt(driver.findElement(By.id("light")).getAttribute("textContent"));
	
			System.out.println("toSend = " + lightCavToSend + " Remaining = " + lightCavRemaining);
			System.out.println("pageNum = " + pageNum + " numPages = " + numPages);
			
			List<WebElement> tbody = driver.findElements(By.tagName("tbody"));
			List<WebElement> trList = tbody.get(tbody.size()-1).findElements(By.tagName("tr"));
			List<WebElement> reportList = trList.subList(2,trList.size()-1);
	
			List<WebElement> farmButtons = driver.findElements(By.className("farm_icon_b"));
			farmButtons = farmButtons.subList(1,farmButtons.size());
			
			assertTrue(farmButtons.size() == reportList.size());
			
			for (int i = 0; i < farmButtons.size(); i++) {
				List<WebElement> tdList = reportList.get(i).findElements(By.tagName("td"));
				Barb farm = new Barb(tdList.get(3).getAttribute("textContent").substring(2,5),tdList.get(3).getAttribute("textContent").substring(6,9));
				boolean hasAttacked = tdList.get(3).findElements(By.tagName("img")).size() != 0;
				boolean isGreen = tdList.get(1).findElements(By.tagName("img")).get(0).getAttribute("src").indexOf("green.png") != -1;
				System.out.print(farm.x + "@" + farm.y);
				
				long travelTime         = (long) (Double.parseDouble(tdList.get(7).getAttribute("textContent")) * 8 * 60);
				Date currentLandingTime = new Date(travelTime + Calendar.getInstance().getTimeInMillis()) ;
				
				if ( isGreen ){
					// Remove from walled
				}
				if( !isGreen ){
					// Add to walled
					System.out.println(" ==> NOT ATTACKED BECAUSE NOT GREEN");
				}
				else if ( hasAttacked ){
					System.out.println(" ==> NOT ATTACKED BECAUSE ALREADY ATTACKED");
					// Check if its not too early to attack
				}
				else{
					// Record currentLandingTime
					boolean passed = false;
					while(!passed){
						try{
							farmButtons.get(i).click();
							passed=true;
						}
						catch(WebDriverException e){
							((JavascriptExecutor)driver).executeScript("window.scrollBy(0, 30);");
						}
					}
					System.out.println(" ==> ATTACKED");
					Thread.sleep(225);
					
					lightCavRemaining -= lightCavToSend;
				}
				
				if(lightCavRemaining < lightCavToSend) break;
			}
			if(lightCavRemaining < lightCavToSend) break;
			boolean passed = false;
			while(!passed){
				try{
					if(select && pageNum > 5) pageNavItems.get(3).click();
					else pageNavItems.get(pageNum).click();
					passed = true;
				}
				catch(WebDriverException e){
					((JavascriptExecutor)driver).executeScript("window.scrollBy(0, 30);");
				}
			}
			wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));
		}
		Thread.sleep(100000);
	}

	void waitForLoad(WebDriver driver) {
		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript(
						"return document.readyState").equals("complete");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(pageLoadCondition);
	}
	
	public class Barb{
		public String x;
		public String y;
		public String id;
		
		public Barb(String x, String y){
			this.x = x;
			this.y = y;
		}
		
		public Barb(String x, String y, String id){
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}
}
