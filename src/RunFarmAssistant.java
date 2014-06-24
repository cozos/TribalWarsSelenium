import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    private static final String GENERATED_DIRECTORY = "generated";
    private static final String BARB_TRACKER_PATH = "generated/BarbTracker";
    private static final String WALLED_BARBS_PATH = "generated/WalledBarbs";
    private static final String CONFIG_PATH = "Config";
    private static final String LINUX32_CHROMEPATH = "chromedriver_linux32/chromedriver";
    @SuppressWarnings("unused")
    private static final String WINDOWS32_CHROMEPATH = "chromedriver_win32\\chromedriver.exe";
    @SuppressWarnings("unused")
    private static final String LINUX64_CHROMEPATH = "chromedriver_linux64/chromedriver";
    
    private static final long MILLISECONDS_IN_HOUR = 3600000;

    private static String USERNAME;
    private static String PASSWORD;
    private static String EMAIL;
    private static long HOURS_BETWEEN_ATTACKS;
    private static long HOURS_BETWEEN_FARMING_RUNS;

    private static ChromeDriverService service;
    private WebDriver driver;
    private WebDriverWait wait;
    private JSONObject trackedBarbs;
    private JSONObject walledBarbs;
    
    @BeforeClass
    public static void createAndStartService() throws IOException {
        generateTempFiles();
        getUserInfo();
        service = new ChromeDriverService.Builder().usingDriverExecutable(new File(LINUX32_CHROMEPATH)).usingAnyFreePort().build();
        service.start();
    }

    @AfterClass
    public static void createAndStopService() {
        service.stop();
    }

    @Before
    public void createDriver() {
        driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
        wait = new WebDriverWait(driver, 15);
    }

    @After
    public void quitDriver() {
        driver.quit();
    }
    
    public static void getUserInfo() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(CONFIG_PATH));
            JSONObject jsonObject = (JSONObject) obj;

            USERNAME = (String) jsonObject.get("username");
            PASSWORD = (String) jsonObject.get("password");
            EMAIL = (String) jsonObject.get("email");
            HOURS_BETWEEN_ATTACKS = (long)(Double.parseDouble((String) jsonObject.get("hoursBetweenAttacks")) * MILLISECONDS_IN_HOUR);
            HOURS_BETWEEN_FARMING_RUNS = (long)(Double.parseDouble((String) jsonObject.get("hoursBetweenFarmingRuns")) * MILLISECONDS_IN_HOUR);
            
        } catch (IOException | ParseException e) {
            System.out.println(e.toString());
        }
    }
    
    public static void generateTempFiles(){
        File generated = new File(GENERATED_DIRECTORY);
        
        if (!generated.exists()) {
            generated.mkdir();
        }
        
        File barbTracker = new File(BARB_TRACKER_PATH);

        if (!barbTracker.exists()) {
            try {
                barbTracker.createNewFile();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        
        File walledBarbs = new File(WALLED_BARBS_PATH);

        if (!walledBarbs.exists()) {
            try {
                walledBarbs.createNewFile();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }
        
    public void openJSON() throws IOException, ParseException{
        if(new File(BARB_TRACKER_PATH).length() != 0)
        {
            setTrackedBarbs((JSONObject) new JSONParser().parse(new FileReader(BARB_TRACKER_PATH)));
        }
        else
        {
            setTrackedBarbs(new JSONObject());
        }
        
        if(new File(WALLED_BARBS_PATH).length() != 0)
        {
        setWalledBarbs((JSONObject) new JSONParser().parse(new FileReader(WALLED_BARBS_PATH)));
        }
        else
        {
            setWalledBarbs(new JSONObject());
        }
    }
    
    public void closeJSON(){
        FileWriter trackerWriter = null;
        try {
            trackerWriter = new FileWriter(new File(BARB_TRACKER_PATH));
            trackerWriter.write(getTrackedBarbs().toJSONString());
            trackerWriter.flush();
        } catch (IOException | NullPointerException e) {
            System.out.println(e.toString());
        }
        finally{
            try {
                trackerWriter.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            setTrackedBarbs(null);
        } 
        
        FileWriter walledBarbWriter = null;
        try {
            walledBarbWriter = new FileWriter(new File(WALLED_BARBS_PATH));
            walledBarbWriter.write(getWalledBarbs().toJSONString());
            walledBarbWriter.flush();
        } catch (IOException | NullPointerException e) {
            System.out.println(e.toString());
        }
        finally{
            try {
                walledBarbWriter.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            setWalledBarbs(null);
        } 
    }
    
    @SuppressWarnings("unchecked")
    public void setTracker(String coordinates, Long arrivalTime) {
        getTrackedBarbs().put(coordinates, arrivalTime);
    }

    @SuppressWarnings("unchecked")
    public boolean getAndSetTracker(String coordinates, Long newArrivalTime){
        if(!getTrackedBarbs().containsKey(coordinates))
        {
            System.out.println(" ==> NOT ATTACKED. ALREADY ATTACKED COULD NOT FIND RECORDS.");
            return false;
        }

        Long oldArrivalTime = (Long) getTrackedBarbs().get(coordinates);
        if (newArrivalTime - oldArrivalTime > HOURS_BETWEEN_ATTACKS) {
            System.out.println(" ==> ATTACKED. DIFFERENCE IS GREATER THAN WHAT YOU SET: " + (int) HOURS_BETWEEN_ATTACKS
                    / MILLISECONDS_IN_HOUR + " hours");
            System.out.println("___________ OLD ATTACK: " + new Date(oldArrivalTime).toString());
            System.out.println("___________ NEW ATTACK: " + new Date(newArrivalTime).toString());
            System.out.println("___________ DIFFERENCE: " + (int) (newArrivalTime - oldArrivalTime) / MILLISECONDS_IN_HOUR + " hours.");
            getTrackedBarbs().put(coordinates, newArrivalTime);
            return true;
        } else {
            System.out.println(" ==> NOT ATTACKED. DIFFERENCE IS LESS THAN WHAT YOU SET: " + (int) HOURS_BETWEEN_ATTACKS
                    / MILLISECONDS_IN_HOUR + " hours");
            System.out.println("___________ OLD ATTACK: " + new Date(oldArrivalTime).toString());
            System.out.println("___________ NEW ATTACK: " + new Date(newArrivalTime).toString());
            System.out.println("___________ DIFFERENCE: " + (int) (newArrivalTime - oldArrivalTime) / MILLISECONDS_IN_HOUR + " hours.");
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    public void addWalledBarb(String coordinates){
        getWalledBarbs().put(coordinates, Calendar.getInstance().getTime());
    }
    
    public void removeWalledBarb(String coordinates){
        getWalledBarbs().remove(coordinates);
    }
    
    public boolean checkIfWalled(String coordinates){
        return getWalledBarbs().containsKey(coordinates);
    }

    private void login() throws WebDriverException, NoSuchElementException {
        WebElement loginName = driver.findElement(By.id("user"));
        loginName.sendKeys(USERNAME);

        WebElement loginPassword = driver.findElement(By.id("password"));
        loginPassword.sendKeys(PASSWORD);

        List<WebElement> loginButton = driver.findElements(By.className("login_button"));
        loginButton.get(0).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("active_server")));

        WebElement serverButton = driver.findElement(By.id("active_server")).findElements(By.className("clearfix")).get(0).findElements(By.tagName("a")).get(0);
        serverButton.click();
    }
    
    private void checkPromoPopup() {
        try
        {
        WebElement gayAssPopUp = driver.findElement(By.id("promo-popup-quit"));
        gayAssPopUp.click();
        }
        catch (WebDriverException e)
        {
            System.out.println(e.toString());
        }
    }

    private void goToFarmAssistant() throws WebDriverException {
        WebElement goToFarmAssistant = driver.findElements(By.className("manager_icon")).get(0);
        goToFarmAssistant.click();
    }

    private void clickButtons() throws InterruptedException, WebDriverException, NullPointerException {
        int firstPageNum = Integer.parseInt(driver.findElements(By.tagName("Strong")).get(0).getAttribute("textContent").substring(2, 3)) - 1;
        int numPages = driver.findElements(By.className("paged-nav-item")).size();
        boolean select;
        
        try {
            select = driver.findElement(By.id("select")) != null;
        } 
        catch (NoSuchElementException e) 
        {
            select = false;
        }

        for (int pageNum = firstPageNum; pageNum < numPages; pageNum++) {
            List<WebElement> pageNavItems = driver.findElements(By.className("paged-nav-item"));

            int lightCavToSend = Integer.parseInt(driver.findElements(By.name("light")).get(1).getAttribute("value"));
            int lightCavRemaining = Integer.parseInt(driver.findElement(By.id("light")).getAttribute("textContent"));

            List<WebElement> tbody = driver.findElements(By.tagName("tbody"));
            List<WebElement> trList = tbody.get(tbody.size() - 1).findElements(By.tagName("tr"));
            List<WebElement> reportList = trList.subList(2, trList.size() - 1);

            List<WebElement> farmButtons = driver.findElements(By.className("farm_icon_b"));
            farmButtons = farmButtons.subList(1, farmButtons.size());

            assertTrue(farmButtons.size() == reportList.size());

            for (int i = 0; i < farmButtons.size(); i++) {
                List<WebElement> tdList = reportList.get(i).findElements(By.tagName("td"));
                
                Barb farm = new Barb(tdList.get(3).getAttribute("textContent").substring(2, 5), tdList.get(3).getAttribute("textContent").substring(6, 9));
                String barb = farm.x + "@" + farm.y;
                System.out.print(barb);
                
                boolean hasAttacked = tdList.get(3).findElements(By.tagName("img")).size() != 0;
                boolean isGreen = tdList.get(1).findElements(By.tagName("img")).get(0).getAttribute("src").indexOf("green.png") != -1;
               
                long travelTime = (long) (Double.parseDouble(tdList.get(7).getAttribute("textContent")) * 10 * 60 * 1000);
                Date currentLandingTime = new Date(travelTime + Calendar.getInstance().getTimeInMillis());
                

                if (isGreen) {
                   removeWalledBarb(barb);
                }
                if (!isGreen) {
                    addWalledBarb(barb);
                    System.out.println(" ==> NOT ATTACKED BECAUSE NOT GREEN");
                }
                else if (hasAttacked) 
                {
                    if(getAndSetTracker(barb, currentLandingTime.getTime()))
                    {
                        boolean passed = false;
                        while (!passed) 
                        {
                            try 
                            {
                                farmButtons.get(i).click();
                                passed = true;
                            } 
                            catch (WebDriverException e) 
                            {
                                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 30);");
                            }
                        }
                        Thread.sleep(225);
                        lightCavRemaining -= lightCavToSend;
                    }
                } 
                else 
                {
                    setTracker(barb, currentLandingTime.getTime());
                    boolean passed = false;
                    while (!passed) 
                    {
                        try 
                        {
                            farmButtons.get(i).click();
                            passed = true;
                        } 
                        catch (WebDriverException e) 
                        {
                            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 30);");
                        }
                    }
                    System.out.println(" ==> ATTACKED. LANDING DATE IS: " + currentLandingTime.toString() );
                    Thread.sleep(225);

                    lightCavRemaining -= lightCavToSend;
                }

                if (lightCavRemaining < lightCavToSend)
                    break;
            }
            
            if (lightCavRemaining < lightCavToSend) break;
            
            boolean passed = false;
            
            while (!passed)
            {
                try 
                {
                    if (select && pageNum > 5) pageNavItems.get(3).click();
                    else pageNavItems.get(pageNum).click();
                    passed = true;
                } 
                catch (WebDriverException e) 
                {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 30);");
                }
            }
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));
        }
    }

    @Test
    public void farm(){
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
            
            openJSON();
            
            driver.get("http://www.tribalwars.net");

            waitForLoad(driver);
            
            login();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("manager_icon")));
            
            checkPromoPopup();

            goToFarmAssistant();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));
            
            clickButtons();
            
            System.out.println("Done.");
            
            Thread.sleep(100000);
        } 
        catch (WebDriverException | InterruptedException | IOException | ParseException | NullPointerException e) 
        {
            System.out.println("Something fucked up.");
            System.out.println(e.toString());
        } 
        finally{
            closeJSON();
        }
    }

    void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

    public JSONObject getTrackedBarbs() {
        return trackedBarbs;
    }

    public void setTrackedBarbs(JSONObject trackedBarbs) {
        this.trackedBarbs = trackedBarbs;
    }

    public JSONObject getWalledBarbs() {
        return walledBarbs;
    }

    public void setWalledBarbs(JSONObject walledBarbs) {
        this.walledBarbs = walledBarbs;
    }

    public class Barb {
        public String x;
        public String y;
        public String id;

        public Barb(String x, String y) {
            this.x = x;
            this.y = y;
        }

        public Barb(String x, String y, String id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }
    }
}
