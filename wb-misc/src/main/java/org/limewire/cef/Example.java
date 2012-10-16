package org.limewire.cef;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class Example {
	
	public static void main(String[] args) throws Exception {
		
		CefContext.initialize(null);
		
		CefHandler cefHandler = new CefHandler() {
			@Override
			public void handleTitleChange(CefBrowser browser, String title) {
				System.out.println("handleTitleChange: " + title);
			}
			
			@Override
			public void handleJSBinding(CefBrowser browser, CefFrame frame,
					CefV8Value object) {
				System.out.println("handleJSBinding: " + frame + ": " + object);
				/*
				// Add a "window.test" object.
				CefV8Value test_obj = CefContext.createV8Object(null);
				object.setValue("test", test_obj);
				
				// Add a "showMessage" function to the "window.test" object.
				test_obj.setValue("showMessage",
					CefContext.createV8Function("showMessage", new MainV8Handler(main_frame)));
				 */
			}
			
			@Override
			public void handleAfterCreated(CefBrowser browser) {
				System.out.println("handleAfterCreated");
			}
			
			@Override
			public void handleAddressChange(CefBrowser browser, CefFrame frame,
					String url) {
				System.out.println("handleAddressChange: " + url);
			}
		};
		
		CefBrowser browser = CefContext.createBrowser(cefHandler, 
				"file:///C:/Work/WB/wb-test/htdocs/canvas.html");

		/*
		WebElement element = driver.findElement(By.linkText("Cloud Simple"));
		element.click();
		 */
		
		BufferedImage buffImage =  
                new BufferedImage(browser.getWidth(), browser.getHeight(),  
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = buffImage.createGraphics();
        graphics.drawImage(buffImage, 0, 0, browser);
        
        File screenshotFile = new File("test.jpg");
		FileOutputStream out = new FileOutputStream(screenshotFile);
        ImageIO.write(buffImage, "jpg", out);
        out.close();

		Desktop.getDesktop().open(screenshotFile);

		CefContext.shutdown();
	}

}
