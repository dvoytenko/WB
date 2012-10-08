package wb.cere;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import wb.util.IoHelper;

/**
 * http://www.cereproc.com/en/products/cloud
 * http://www.cereproc.com/files/CereVoiceCloudGuide.pdf
 */
public class CereTts {

	public static void main(String[] args) throws Exception {

		String xml = "<?xml version='1.0'?>" +
				"<speakExtended>"
				+ "<accountID>50727aae31c52</accountID>"
				+ "<password>S56HyCi3mw</password>"
				+ "<voice>Isabella</voice>"
				+ "<audioFormat>wav</audioFormat>"
				+ "<text>Or may be you are a professor and would like to explain a formula to a student.</text>" +
				"</speakExtended>";

		URL u = new URL("https://cerevoice.com/rest/rest_1_1.php");
		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type","text/xml; charset=utf-8");
		connection.setRequestProperty("Content-Length", String.valueOf(xml));
		OutputStream out = connection.getOutputStream();
		Writer wout = new OutputStreamWriter(out, "UTF-8");      
		wout.write(xml);  
		wout.flush();
		wout.close();

		InputStream in = connection.getInputStream();
		String resp = IoHelper.readText(in, "UTF-8");
		System.out.println(resp);
		
		InputSource source = new InputSource(new StringReader(resp));
		SAXReader reader = new SAXReader();
		Document doc = reader.read(source);
		String fileUrl = doc.getRootElement().element("fileUrl").getText();
		System.out.println(fileUrl);
		
		IoHelper.readFile(new URL(fileUrl), new File("target/cere1.wav"));
	}

}
