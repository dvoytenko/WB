package wb.selenium;

import java.awt.Desktop;
import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

public class Selenium2Example {

	public static void main(String[] args) throws Exception {
		
		System.setProperty("webdriver.chrome.driver", "C:\\Apps\\Google\\chromedriver\\chromedriver.exe");
		
		ChromeDriverService service = new ChromeDriverService.Builder()
        	// .usingChromeDriverExecutable(new File("C:\\Apps\\Google\\chromedriver\\chromedriver.exe"))
        	.usingDriverExecutable(new File("C:\\Apps\\Google\\chromedriver\\chromedriver.exe"))
        	.usingAnyFreePort()
        	.build();
		service.start();
		
		// Create a new instance of the Firefox driver
		// Notice that the remainder of the code relies on the interface, 
		// not the implementation.
		WebDriver driver = new ChromeDriver(service);

		// And now use this to visit Google
		driver.get("file:///C:/Work/WB/wb-test/htdocs/canvas.html");
		// Alternatively the same thing can be done like this
		// driver.navigate().to("http://www.google.com");

		// Find the text input element by its name
		WebElement element = driver.findElement(By.linkText("Cloud Simple"));

		// Enter something to search for
		element.click();
		
//		Thread.sleep(4000L);
		
		File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		System.out.println(screenshotFile);
		Desktop.getDesktop().open(screenshotFile);

		//Close the browser
//		driver.quit();

		service.stop();
	}

}
