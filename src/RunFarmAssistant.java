import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.SocketClient;
import com.sun.corba.se.impl.orbutil.ObjectWriter;

@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class RunFarmAssistant extends TestCase {

    private static final String GENERATED_DIRECTORY = "generated";
    private static final String BARB_TRACKER_PATH = "generated/BarbTracker";
    private static final String WALLED_BARBS_PATH = "generated/WalledBarbs";
    private static final String LOG_PATH = "log";
    private static final String CAPTCHA_LOG_PATH = "captchasolve";
    private static final String CONFIG_PATH = "Config";
    private static final String IS_RUNNING_PATH = "IsRunning";
    private static final String LINUX32_CHROMEPATH = "chromedriver_linux32/chromedriver";
    private static final String CAPTCHA_USERNAME = "SmallJohnson";
    private static final String CAPTCHA_PASSWORD = "33333";

    private static final long MILLISECONDS_IN_HOUR = 3600000;
    
    private static String USERNAME;
    private static String PASSWORD;
    private static String EMAIL;
    private static long HOURS_BETWEEN_ATTACKS;
    private static long HOURS_BETWEEN_ATTACKS_IF_MAX_LOOTED;
    private static long HOURS_BETWEEN_FARMING_RUNS;

    private static ChromeDriverService service;
    private WebDriver driver;
    private WebDriverWait wait;
    private JSONObject trackedBarbs;
    private JSONObject walledBarbs;
    private static final List<String> yourVillages = new CopyOnWriteArrayList<String>();
    private static final Map<String,VillageSettings> yourVillageSettings = new HashMap<String,VillageSettings>();
    
    @BeforeClass
    public static void createAndStartService() throws IOException {
        generateTempFiles();
        getUserInfo();
        service = new ChromeDriverService.Builder().usingDriverExecutable(new File(LINUX32_CHROMEPATH)).usingAnyFreePort().build();
        service.start();
        setRunning(true);
    }

    @AfterClass
    public static void createAndStopService() {
        service.stop();
    }

    @After
    public void quitDriver() {
        driver.quit();
        System.out.println("[END] Session terminated!");
        setRunning(false);
    }
    
    public void createDriver() {
        driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
        wait = new WebDriverWait(driver, 15);
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
            HOURS_BETWEEN_ATTACKS_IF_MAX_LOOTED = (long)(Double.parseDouble((String) jsonObject.get("hoursBetweenAttacksIfMaxLooted")) * MILLISECONDS_IN_HOUR);
            
        } catch (IOException | ParseException e) {
            System.out.println(e.toString());
        }
    }
    
    public static void setRunning(boolean isRunning){
        FileWriter isRunningWriter = null;
        try {
            isRunningWriter = new FileWriter(new File(IS_RUNNING_PATH),false);
            isRunningWriter.write((isRunning) ? "true" : "false" );
            isRunningWriter.flush();
        }
        catch (IOException | NullPointerException e) {
            System.out.println(e.toString());
        }
        finally
        {
            try {
                isRunningWriter.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
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
        
        
        File captchaLog = new File(CAPTCHA_LOG_PATH);

        if (!captchaLog.exists()) {
            try {
                captchaLog.createNewFile();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        
        File log = new File(LOG_PATH);

        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        
        File isRunning = new File(IS_RUNNING_PATH);

        if (!isRunning.exists()) {
            try {
                isRunning.createNewFile();
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
            if(getTrackedBarbs() == null) throw new NullPointerException();
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
            if(getWalledBarbs() == null) throw new NullPointerException();
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
    public boolean getAndSetTracker(String coordinates, Long newArrivalTime,boolean maxLoot, String village){
        if(!getTrackedBarbs().containsKey(coordinates))
        {
            return false;
        }
        
        Long oldArrivalTime = new Long(0);
        if(getTrackedBarbs().containsKey(coordinates+"_oldArrivalTime")){
            oldArrivalTime = (Long) getTrackedBarbs().get(coordinates+"_oldArrivalTime");
        }
        
        Long currentTime = Calendar.getInstance().getTimeInMillis();
       
        Long previousNewArrivalTime = (Long) getTrackedBarbs().get(coordinates);
        
        // Full Haul Optimization
        if (maxLoot && (currentTime - oldArrivalTime < HOURS_BETWEEN_ATTACKS*2)
                && (newArrivalTime - previousNewArrivalTime > HOURS_BETWEEN_ATTACKS_IF_MAX_LOOTED)) 
        {
            System.out.print(coordinates + " ==> ATTACKED FROM [" + village + "] BECAUSE MAX LOOT. DIFFERENCE: "
                    + (double) Math.round((double) (newArrivalTime - previousNewArrivalTime) / MILLISECONDS_IN_HOUR * 100) / 100
                    + " Hours IS GREATER THAN WHAT YOU SET WHEN MAX LOOTED: "
                    + (double) Math.round((double) HOURS_BETWEEN_ATTACKS_IF_MAX_LOOTED / MILLISECONDS_IN_HOUR * 100) / 100
                    + " Hours. Time of max loot was "
                    + (double) Math.round((double) (currentTime - oldArrivalTime) / MILLISECONDS_IN_HOUR * 100) / 100
                    + " hours ago. ");
            getTrackedBarbs().put(coordinates, newArrivalTime);
            getTrackedBarbs().put(coordinates + "_oldArrivalTime", previousNewArrivalTime);
            return true;
        } 
        else if (newArrivalTime - previousNewArrivalTime > HOURS_BETWEEN_ATTACKS) 
        {
            System.out.print(coordinates + " ==> ATTACKED FROM [" + village + "]. DIFFERENCE: "
                    + (double) Math.round((double) (newArrivalTime - previousNewArrivalTime) / MILLISECONDS_IN_HOUR * 100) / 100
                    + " Hours IS GREATER THAN WHAT YOU SET: "
                    + (double) Math.round((double) HOURS_BETWEEN_ATTACKS / MILLISECONDS_IN_HOUR * 100) / 100 + " Hours. ");
            getTrackedBarbs().put(coordinates, newArrivalTime);
            getTrackedBarbs().put(coordinates + "_oldArrivalTime", previousNewArrivalTime);
            return true;
        } else {
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
        
        ((JavascriptExecutor) driver).executeScript("Index.submit_login('server_en75');");
        
    }
    
    private void getSettings() throws ParseException {
        WebElement villageLink = driver.findElement(By.id("menu_row2_village")).findElements(By.tagName("a")).get(0);

        villageLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("village_note")));
        WebElement note = driver.findElement(By.id("village_note"));

        String jsonString = note.getAttribute("textContent");
        JSONObject jsonSettings = (JSONObject) new JSONParser().parse(jsonString);

        for (int i = 0; i < yourVillages.size(); i++) {
            int villageNum = i + 1;
            VillageSettings settings = new VillageSettings(Integer.parseInt((String) jsonSettings.get(villageNum + "LC")),
                    Integer.parseInt((String) jsonSettings.get(villageNum + "HC")));
            System.out.println("[SETTINGS] LC TO KEEP: " + jsonSettings.get(villageNum + "LC") + ", HC TO KEEP:"
                    + jsonSettings.get(villageNum + "HC") + " in [" + yourVillages.get(i) + "]");
            yourVillageSettings.put(yourVillages.get(i), settings);
        }
    }
    
    private void checkPromoPopup() {
        try
        {
        WebElement gayAssPopUp = driver.findElement(By.id("promo-popup-quit"));
        gayAssPopUp.click();
        }
        catch (WebDriverException e)
        {
        }
    }

    private void goToFarmAssistant() throws WebDriverException, ParseException {
        try{
            WebElement overview = driver.findElements(By.className("modemenu")).get(0).findElements(By.tagName("td")).get(0)
                    .findElements(By.tagName("a")).get(0);
            
            overview.click();
            
            List<WebElement> villageNames = driver.findElements(By.className("quickedit-label"));
            
            System.out.println("[VILLAGE] List of your villages: ");
            int i = 1;
            for(WebElement villageName : villageNames){
                String rawVillageName = villageName.getAttribute("textContent");
                String processedVillageName = rawVillageName.substring(0,rawVillageName.indexOf("(")).trim();
                yourVillages.add(processedVillageName);
                System.out.println("[VILLAGE " + i + "] " + processedVillageName);
                i++;
            }
            //yourVillages.remove("1. Rectal Rummager Rocko");
        }
        catch(NoSuchElementException e){
            yourVillages.add(driver.findElement(By.id("menu_row2_village")).getAttribute("textContent").trim());
            System.out.println("[ONLY VILLAGE] " + driver.findElement(By.id("menu_row2_village")).getAttribute("textContent").trim());
        }
        
        getSettings();
        
        WebElement goToFarmAssistant = driver.findElements(By.className("manager_icon")).get(0);
        goToFarmAssistant.click();
    }
    
    private void clickButtons(int pageNum) throws InterruptedException, WebDriverException, NullPointerException{
        for(String village : yourVillages){
            checkAndSolveCaptcha();
            
            while(!village.equals(driver.findElement(By.id("menu_row2_village")).getAttribute("textContent").trim())){
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
                driver.findElement(By.id("village_switch_right")).click();
                waitForLoad(driver);
            }
            System.out.println("[VILLAGE] Page " + ((int)pageNum+1) + ". DOING VILLAGE: [" + village + "]");
            
            int lightCavToSend = Integer.parseInt(driver.findElements(By.name("light")).get(1).getAttribute("value"));
            int lightCavRemaining = Integer.parseInt(driver.findElement(By.id("light")).getAttribute("textContent"));
            
            int heavyCavToSend = Integer.parseInt(driver.findElements(By.name("heavy")).get(0).getAttribute("value"));
            int heavyCavRemaining = Integer.parseInt(driver.findElement(By.id("heavy")).getAttribute("textContent"));
            
            System.out.println("[INFO] You have " + lightCavRemaining + " LC. Sending " + lightCavToSend + " LC each barb.");
            System.out.println("[INFO] You have " + heavyCavRemaining + " HC. Sending " + heavyCavToSend + " HC each barb.");
            
            List<WebElement> tbody = driver.findElements(By.tagName("tbody"));
            List<WebElement> trList = tbody.get(tbody.size() - 1).findElements(By.tagName("tr"));
            List<WebElement> reportList = trList.subList(2, trList.size() - 1);
    
            List<WebElement> farmButtons = driver.findElements(By.className("farm_icon_b"));
            farmButtons = farmButtons.subList(1, farmButtons.size());
            
            List<WebElement> heavyFarmButtons = driver.findElements(By.className("farm_icon_a"));
            heavyFarmButtons = heavyFarmButtons.subList(1, heavyFarmButtons.size());
    
            assertTrue(farmButtons.size() == reportList.size());
            for (int i = 0; i < farmButtons.size(); i++) {
                List<WebElement> tdList = reportList.get(i).findElements(By.tagName("td"));
                
                Barb farm = new Barb(tdList.get(3).getAttribute("textContent").substring(2, 5), tdList.get(3).getAttribute("textContent").substring(6, 9));
                String barb = farm.x + "@" + farm.y;
                //System.out.print(barb + " ");
                
                boolean hasAttacked = tdList.get(3).findElements(By.tagName("img")).size() != 0;
                boolean maxLoot = tdList.get(2).findElements(By.tagName("img")).get(0).getAttribute("src").indexOf("1.png") != -1;
                boolean isGreen = tdList.get(1).findElements(By.tagName("img")).get(0).getAttribute("src").indexOf("green.png") != -1;
                boolean hcMode = lightCavRemaining < yourVillageSettings.get(village).lcToKeep + lightCavToSend;
                
                long travelTime = (long) (Double.parseDouble(tdList.get(7).getAttribute("textContent")) * 10 * 60 * 1000);
                long heavyTravelTime = (long) (Double.parseDouble(tdList.get(7).getAttribute("textContent")) * 11 * 60 * 1000);
                Date currentLandingTime = new Date(travelTime + Calendar.getInstance().getTimeInMillis());
                Date heavyCurrentLandingTime = new Date(heavyTravelTime + Calendar.getInstance().getTimeInMillis());
                
                if(hcMode) currentLandingTime = heavyCurrentLandingTime;
                
                if (isGreen) {
                   removeWalledBarb(barb);
                }
                if (!isGreen) {
                    addWalledBarb(barb);
                    //System.out.println("==> NOPE.");
                }
                else if (hasAttacked) 
                {
                    if(getAndSetTracker(barb, currentLandingTime.getTime(), maxLoot,village))
                    {
                        boolean passed = false;
                        while (!passed) 
                        {
                            try 
                            {   if(!hcMode)
                                {
                                    farmButtons.get(i).click();
                                }
                                else
                                {
                                    heavyFarmButtons.get(i).click();
                                }
                                passed = true;
                            } 
                            catch (WebDriverException e) 
                            {
                                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 30);");
                            }
                        }
                        if(!hcMode)
                        {
                            lightCavRemaining -= lightCavToSend;
                            System.out.println("Remaining LC: " + lightCavRemaining);
                        }
                        else
                        {
                            heavyCavRemaining -= heavyCavToSend;
                            System.out.println("Remaining HC: " + heavyCavRemaining);
                        }
                        Thread.sleep(225);
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
                            if (!hcMode)
                            {
                                farmButtons.get(i).click();
                            }
                            else
                            {
                                heavyFarmButtons.get(i).click();
                            }
                            passed = true;
                        } 
                        catch (WebDriverException e) 
                        {
                            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 30);");
                        }
                    }
                    if (!hcMode)
                    {
                        lightCavRemaining -= lightCavToSend;
                        System.out.println(barb + " ==> SENT LC FROM [" + village + "]. LANDING DATE IS: " + currentLandingTime.toString() + ". LC = " + lightCavRemaining + ".");
                    }
                    else
                    {
                        heavyCavRemaining -= heavyCavToSend;
                        System.out.println(barb + " ==> SENT HC FROM [" + village + "]. LANDING DATE IS: " + currentLandingTime.toString() + ". HC = " + heavyCavRemaining + ".");
                   
                    }
                    Thread.sleep(225);
                }
                
                if (lightCavRemaining < yourVillageSettings.get(village).lcToKeep + lightCavToSend &&
                        heavyCavRemaining < yourVillageSettings.get(village).hcToKeep + heavyCavToSend ){
                    System.out.println("[" + village + "] ran out of Cavalry at page " + ((int)pageNum+1) + ". " + lightCavRemaining + " LC remaining.");
                    yourVillages.remove(village);
                    break;
                }
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            try{
                driver.findElement(By.id("village_switch_right")).click();
            }
            catch(NoSuchElementException e){
                //no-op
            }
        }
    }

    private void doFarmAssistant() throws InterruptedException, WebDriverException, NullPointerException {
        
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

        for (int pageNum = 0; pageNum < numPages+1; pageNum++) {
            System.out.println("[PAGE] At page: " + ((int)pageNum+1) + ".");
            List<WebElement> pageNavItems = driver.findElements(By.className("paged-nav-item"));
            
            clickButtons(pageNum);
            
            if (pageNum == numPages || yourVillages.isEmpty()) break;
            
            boolean passed = false;
            while (!passed)
            {
                try 
                {   
                    pageNavItems = driver.findElements(By.className("paged-nav-item"));
                    if (select && pageNum > 5) pageNavItems.get(3).click();
                    else pageNavItems.get(pageNum).click();
                    passed = true;
                } 
                catch (WebDriverException e) 
                {   
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 100);");
                }
            }
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));
        }
    }

    @Test
    public void farm(){
        System.out.println("[START] RunFarmAssistant bot started.");
        while(true){
            Boolean jsonClosed = false;
            Boolean driverClosed = false;
            try {
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
                
                openJSON();
                jsonClosed = false;
                
                System.out.println("[TRY] Opening browser...");
                createDriver();
                driver.get("http://www.tribalwars.net");
                driverClosed = false;
                System.out.println("[SUCCESS] Opened browser!");
                
                waitForLoad(driver);
                
                System.out.println("[TRY] Logging in to " + USERNAME + "...");
                login();
                System.out.println("[SUCCESS] Logged in!");
                
                checkAndSolveCaptcha();
                
                checkPromoPopup();
                
                checkAndSolveCaptcha();
   
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("manager_icon")));
                
                System.out.println("[TRY] Going in farm assistant...");
                goToFarmAssistant();
                System.out.println("[SUCCESS] In farm assistant!");
                
                checkAndSolveCaptcha();
    
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("farm_icon_b ")));
                
                System.out.println("[TRY] Clicking buttons...");
                doFarmAssistant();
                
                System.out.println("[SUCCESS] Sent All LC.");
         
                closeJSON();
                jsonClosed = true;
                
                System.out.println("[TRY] Closing browser...");
                driver.close();
                driverClosed = true;
                System.out.println("[SUCCESS] Closed browser!");
                
                System.out.print("[END] Will run again at SERVER TIME: " + new Date(HOURS_BETWEEN_FARMING_RUNS + Calendar.getInstance().getTimeInMillis()).toString());
                System.out.println(" (" + (double)Math.round((double)HOURS_BETWEEN_FARMING_RUNS / MILLISECONDS_IN_HOUR * 100) / 100 + " hours from now)");
                System.out.println("[REMINDER] EST is 5 hours behind server time.");
                System.out.println("[REMINDER] Will rerun at LOCAL TIME(EST): "  + new Date(HOURS_BETWEEN_FARMING_RUNS + Calendar.getInstance().getTimeInMillis() - (5*MILLISECONDS_IN_HOUR)).toString());  
                
                Thread.sleep(HOURS_BETWEEN_FARMING_RUNS);
            } 
            catch (Exception e ) 
            {
                System.out.println("[FAILED] Something fucked up.");
                System.out.println(e.toString());
                System.out.println("[FAILED] Try again");
                continue;
            } 
            finally{
                if(!jsonClosed) closeJSON();
                if(!driverClosed) driver.close();
                yourVillages.clear();
                yourVillageSettings.clear();
                System.out.println("[NEW] Farming Again. SERVER TIME: " + Calendar.getInstance().getTime().toString());
                System.out.println("[REMINDER] EST is 5 hours behind server time.");
                System.out.println("[REMINDER] LOCAL TIME(EST): "  + new Date(Calendar.getInstance().getTimeInMillis() - (5*MILLISECONDS_IN_HOUR)).toString());
            }
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
    
    private void captchaLogger(String logString){
        FileWriter captchaWriter = null;
        try {
            captchaWriter = new FileWriter(new File(CAPTCHA_LOG_PATH),true);
            captchaWriter.write(logString+"\n");
        } catch (IOException | NullPointerException e) {
            //no-op
        }
        finally{
            try {
                captchaWriter.close();
            } catch (IOException e) {
                //no-op
            }
            System.out.println(logString);
        } 
    }
    public void checkAndSolveCaptcha(){
        System.out.println("[CAPTCHA] Checking for captchas...");
        System.out.println("[CAPTCHA] ===============START: " + Calendar.getInstance().getTime().toString());
        Captcha captcha = null;
        
        boolean alreadyTried = false;
        while(true)
        {
            Client captchaClient = (Client)(new SocketClient(CAPTCHA_USERNAME, CAPTCHA_PASSWORD));
            captchaClient.isVerbose = true;
            
            WebElement botCheckImage = null; 
            Boolean botCheckImageDisplayed = false;
            
            try
            {
                botCheckImage = driver.findElement(By.id("bot_check_image"));
                
                botCheckImageDisplayed = (Boolean) ((JavascriptExecutor) driver)
                        .executeScript(
                                "return arguments[0].offsetWidth > 0 &&  arguments[0].offsetHeight > 0",
                                botCheckImage);
                
                if(alreadyTried && botCheckImageDisplayed)
                {
                    captchaLogger("[CAPTCHASOLVE] Solved captcha was wrong.");
                    try 
                    {
                        if (captchaClient.report(captcha)) 
                        {
                            captchaLogger("[CAPTCHASOLVE] Reported as incorrectly solved");
                        }
                        else
                        {
                            captchaLogger("[CAPTCHASOLVE] Failed reporting incorrectly solved CAPTCHA");
                        }
                    }
                    catch (IOException | com.DeathByCaptcha.Exception e) 
                    {
                        captchaLogger("[CAPTCHASOLVE] Failed reporting incorrectly solved CAPTCHA: " + e.toString());
                    }
                    alreadyTried = false;   
                    captchaLogger("[CAPTCHASOLVE] Retrying to solve captcha...");
                }
            }
            catch (NoSuchElementException e)
            {
                if(alreadyTried) captchaLogger("[CAPTCHASOLVE] CAPTCHA was solved.");
                System.out.println("[CAPTCHA]================END  : " + Calendar.getInstance().getTime().toString());
                if(!alreadyTried) System.out.println("[CAPTCHA] No captchas found. Moving along.");
                return;
            }
            
            
            if(botCheckImage == null || !botCheckImageDisplayed){
                if(alreadyTried) captchaLogger("[CAPTCHASOLVE] CAPTCHA was solved.");
                System.out.println("[CAPTCHA]================END  : " + Calendar.getInstance().getTime().toString());
                if(!alreadyTried) System.out.println("[CAPTCHA] No captchas. Moving along.");
                return;
            } 
            
            captchaLogger("[CAPTCHASOLVE] Found captcha. Attempting to solve...");
            
            try
            {
            captchaLogger("[CAPTCHASOLVE] Remaining balance for " + CAPTCHA_USERNAME + ": " + captchaClient.getBalance() + " US cents");
            }
            catch (IOException | com.DeathByCaptcha.Exception e) 
            {
                captchaLogger("[CAPTCHASOLVE] Failed fetching balance. Try again");
                continue;
            }
            
            try 
            {   
                captchaLogger("[CAPTCHASOLVE] Trying to upload CAPTCHA...");
                byte[] captchaScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                BufferedImage wholeScreen = ImageIO.read(new ByteArrayInputStream(captchaScreenshot));
                Dimension captchaDimension = botCheckImage.getSize();
                Point captchaLocation = botCheckImage.getLocation();
                BufferedImage captchaImage = wholeScreen.getSubimage(captchaLocation.x, captchaLocation.y, captchaDimension.width,
                        captchaDimension.height);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(captchaImage, "png", os);
                captcha = captchaClient.decode(new ByteArrayInputStream(os.toByteArray()), 300);
                captchaLogger("[CAPTCHASOLVE] Captcha upload SUCCESS.");
                captchaLogger("[CAPTCHASOLVE] Trying to solve CAPTCHA...");
            } 
            catch (IOException | com.DeathByCaptcha.Exception | InterruptedException e) 
            {
                captchaLogger("[CAPTCHASOLVE] Failed uploading CAPTCHA.");
                captchaLogger("[CAPTCHASOLVE] " + e.toString());
                captchaLogger("[CAPTCHASOLVE] Try again.");
                continue;
            }
            
            if (null != captcha) {
                captchaLogger("[CAPTCHASOLVE] CAPTCHA " + captcha.id + " solved: " + captcha.text);
                captchaLogger("[CAPTCHASOLVE] Trying to submit CAPTCHA...");
                
                try
                {
                captchaLogger("[CAPTCHASOLVE] Trying to submit to component captcha...");
                WebElement captchaform = driver.findElement(By.id("bot_check_form")); 
                WebElement captchaInput = driver.findElement(By.id("bot_check_code"));
                WebElement captchaSubmit = driver.findElement(By.id("bot_check_submit"));
                
                captchaInput.sendKeys(captcha.text);
                captchaSubmit.submit();
                alreadyTried = true;
                captchaLogger("[CAPTCHASOLVE] Component Captcha Submitted.");
                }
                catch(NoSuchElementException e)
                {
                    captchaLogger("[CAPTCHASOLVE] It's not component captcha... ");
                    captchaLogger("[CAPTCHASOLVE] Because exception:");
                    captchaLogger("[CAPTCHASOLVE] EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                    System.out.println("[CAPTCHASOLVE] " + e.toString());
                    captchaLogger("[CAPTCHASOLVE] EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                    captchaLogger("[CAPTCHASOLVE] Trying fullscreen captcha...");
                    boolean captchaSubmitted = false;
                    while(!captchaSubmitted)
                    {
                        try
                        {                           
                        List<WebElement> captchaInputs = driver.findElements(By.tagName("input"));
                        WebElement captchaText = captchaInputs.get(0);
                        WebElement captchaSubmitButton = captchaInputs.get(1);
                    
                        captchaText.sendKeys(captcha.text);
                        captchaSubmitButton.submit();
                        Thread.sleep(2000);
                        captchaLogger("[CAPTCHASOLVE] Fullscreen Captcha Submitted.");
                        alreadyTried = true;
                        captchaSubmitted = true;
                        }
                        catch(Exception e1)
                        {
                            captchaLogger("[CAPTCHASOLVE] Failed submitting CAPTCHA " + e1.toString());
                            captchaLogger("[CAPTCHASOLVE] Try again.");
                        }
                    }
                }
            } 
            else 
            {
                captchaLogger("[CAPTCHASOLVE] Failed solving CAPTCHA");
                captchaLogger("[CAPTCHASOLVE] Try again.");
                continue;
            }
        }
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
    
    public class VillageSettings {
        public int lcToKeep;
        public int hcToKeep;

        public VillageSettings(int lcToKeep, int hcToKeep){
            this.lcToKeep = lcToKeep;
            this.hcToKeep = hcToKeep;
        }
    }
}
