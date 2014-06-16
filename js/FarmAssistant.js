// ==UserScript==
// @name       Tribal Wars Farm Assistant Utility
// @namespace  http://meatspin.com
// @version    0.1
// @description  yes
// @match      http://*/*
// @copyright  2014, Arwin
// ==/UserScript==


String.prototype.contains = function(string, from){
    return this.indexOf(string, from) >= 0;
}

if((window.location.href.contains("http://en75.tribalwars.net/game.php?village=") && window.location.href.contains("&order=distance&dir=asc&Farm_page")) || (window.location.href.contains("http://en75.tribalwars.net/game.php?village=") && !window.location.href.contains("&order=distance&dir=asc&Farm_page") && window.location.href.contains("&screen=am_farm"))){
var lightCavToSend = document.getElementsByName("light")[1].value;
var numLightCav = document.getElementById("light").textContent;
}
var twoHoursInMilliseconds = 4500000; // smart send interval time
var SMART_SEND_ENABLED = true;

function simulatedClick(target, options) {
    var event = target.ownerDocument.createEvent('MouseEvents');
    event.initMouseEvent('click',true,true,target.ownerDocument.defaultView,1,0,0,0,0,false,false,false,false,0,null);    
    target.dispatchEvent(event);
}

function pausecomp(millis) 
{
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); } 
    while(curDate-date < millis);
} 

function monthToNum(monthStr){
    if(monthStr == "Jan") return 0;
    else if(monthStr == "Feb") return 1;
    else if(monthStr == "Mar") return 2;
    else if(monthStr == "Apr") return 3;
    else if(monthStr == "May") return 4;
    else if(monthStr == "Jun") return 5;
    else if(monthStr == "Jul") return 6;
    else if(monthStr == "Aug") return 7;
    else if(monthStr == "Sep") return 8;
    else if(monthStr == "Oct") return 9;
    else if(monthStr == "Nov") return 10;
    else if(monthStr == "Dec") return 11;        
}

function gayAssDateString(stringDate){
    var dateArray = stringDate.split(/[ ,]/);
    month = monthToNum(dateArray[1]);
    var hourMinuteSecond = dateArray[4].split(/[:]/);
    var realDate = new Date(dateArray[3],month,dateArray[2],hourMinuteSecond[0],hourMinuteSecond[1],hourMinuteSecond[2],0);
    return realDate;
}

function clickButtons(){
    for (var i=1;i<document.getElementsByClassName("farm_icon_b").length-1;i++)
    {
        if(parseInt(numLightCav) < parseInt(lightCavToSend)) break;
        var reports = document.getElementsByTagName("tbody")[document.getElementsByTagName("tbody").length-1].querySelectorAll(".row_a,.row_b")[i-1];
        var coordinates = reports.cells[3].textContent.substring(2,5) + "@" + reports.cells[3].textContent.substring(6,9);
        var distance = parseFloat(reports.cells[7].textContent);
        var travelTimeInSeconds = 8*distance*60; // Light Cavalry travels 8minutes/field
        var curDate = new Date();
        var landingDate = new Date(curDate.getFullYear(),curDate.getMonth(),curDate.getDate(),curDate.getHours(),curDate.getMinutes(),curDate.getSeconds()+travelTimeInSeconds,curDate.getMilliseconds());
        if(window.localStorage.getItem("WalledBarbs") == null) window.localStorage.setItem("WalledBarbs","Walled Barbs");
        var walledBarbs = window.localStorage.getItem("WalledBarbs");
        pausecomp((Math.floor((Math.random()*10) + 200)));
        	console.log(i);
        	if(reports.innerHTML.search("dots/green.png") != -1){
            	var fixedBarb = coordinates + ",";
                walledBarbs = walledBarbs.replace(fixedBarb,"");
                window.localStorage.setItem("WalledBarbs",walledBarbs);
        	}
        	if(reports.innerHTML.search("dots/green.png") == -1){
                //TODO : add to localstorage
                if(!walledBarbs.contains(coordinates)){
                walledBarbs = walledBarbs + "," + coordinates;
                window.localStorage.setItem("WalledBarbs",walledBarbs);
                }
                console.log(window.localStorage.getItem("WalledBarbs"));
          	}
            else if(reports.innerHTML.search("attack.png") != -1){
                //check if theres next hit lands in 2 h
                if(SMART_SEND_ENABLED){               
                    if(window.localStorage.getItem(coordinates) != null){
                        var oldLandingDate = gayAssDateString(window.localStorage.getItem(coordinates));
                        console.log("Tentative Landing Date: " + landingDate);
                        console.log("Landing date in transit: " + oldLandingDate);
                        console.log("Difference: " + ((landingDate-oldLandingDate)/3600000).toFixed(2) + " hours");
                        if((landingDate - oldLandingDate) >= twoHoursInMilliseconds){
                              console.log("Attack anyways to " + coordinates);
                              simulatedClick(document.getElementsByClassName("farm_icon_b")[i]);
                              window.localStorage.setItem(coordinates,landingDate);
                              numLightCav = numLightCav - 3;
                             
                        }
                        else  console.log("Too early to attack " + coordinates);
                    }
                }
                console.log("");
            }
            else{
                console.log("Sent attack to untouched barb " + coordinates);
                simulatedClick(document.getElementsByClassName("farm_icon_b")[i]);
            	window.localStorage.setItem(coordinates,landingDate);
                numLightCav = numLightCav - 3;
            }
        console.log();
        
    }
}

if(window.location.href.contains("http://en75.tribalwars.net/game.php?village=") && !window.location.href.contains("&order=distance&dir=asc&Farm_page") && window.location.href.contains("&screen=am_farm") && document.getElementById("light").textContent > 2){
    if(confirm("Run Script?")){
        clickButtons();
        pausecomp(200);
        if(document.getElementsByClassName("paged-nav-item")[0] != undefined && (parseInt(numLightCav) >= parseInt(lightCavToSend)))simulatedClick(document.getElementsByClassName("paged-nav-item")[0]);
    }
}

if(window.location.href.contains("http://en75.tribalwars.net/game.php?village=") && window.location.href.contains("&order=distance&dir=asc&Farm_page") && document.getElementById("light").textContent > 2){
    var select = $('select');
	var page_loc = window.location.href.search("Farm_page")+10;
	var pageNum = parseInt(window.location.href.substring(page_loc,page_loc+3));
	pausecomp(200);
    clickButtons();
    if(pageNum>5 && select != null) pageNum = 3; // Only if you have 20 pages or higher
	if(document.getElementsByClassName("paged-nav-item")[pageNum] != undefined && (parseInt(numLightCav) >= parseInt(lightCavToSend)))simulatedClick(document.getElementsByClassName("paged-nav-item")[pageNum]); 
}
 