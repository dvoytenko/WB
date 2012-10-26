package wb.cere;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wb.model.TtsEngine;
import wb.model.Voice;
import wb.util.IoHelper;
import wb.util.XmlHelper;

public class CereEngine implements TtsEngine {

	@Override
	public String getId() {
		return "cere";
	}

	@Override
	public String getName() {
		return "Cere";
	}

	@Override
	public List<Voice> getVoices() {
		List<Voice> list = new ArrayList<Voice>();
		list.add(new Voice("Isabella", "Isabella", "Female US", "female", "en", "US", "en_US"));
		list.add(new Voice("Adam", "Adam", "Male US", "male", "en", "US", "en_US"));
		return list;
	}

	@Override
	public String getDefaultVoiceId() {
		return "Isabella";
	}

	@Override
	public void generateAudio(String text, String voiceId, OutputStream output)
			throws IOException {
		
		try {
			
			Document request = XmlHelper.newDocument();
			{
				Element root = request.createElement("speakExtended");
				request.appendChild(root);
				{
					// accountID
					Element accountId = request.createElement("accountID");
					root.appendChild(accountId);
					accountId.appendChild(request.createTextNode("50727aae31c52"));

					// password
					Element pass = request.createElement("password");
					root.appendChild(pass);
					pass.appendChild(request.createTextNode("S56HyCi3mw"));
					
					// voice
					if (voiceId != null) {
						Element voiceEl = request.createElement("voice");
						root.appendChild(voiceEl);
						voiceEl.appendChild(request.createTextNode(voiceId));
					}
					
					// audioFormat
					Element formatEl = request.createElement("audioFormat");
					root.appendChild(formatEl);
					formatEl.appendChild(request.createTextNode("wav"));
					
					// text
					Element textEl = request.createElement("text");
					root.appendChild(textEl);
					textEl.appendChild(request.createTextNode(text));
				}
			}
			
			String requestXml = XmlHelper.toXml(request);
			System.out.println(requestXml);
			
			URL u = new URL("https://cerevoice.com/rest/rest_1_1.php");
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type","text/xml; charset=utf-8");
			OutputStream out = connection.getOutputStream();
			Writer wout = new OutputStreamWriter(out, "UTF-8");      
			wout.write(requestXml);  
			wout.flush();
			wout.close();

			InputStream in = connection.getInputStream();
			String resp = IoHelper.readText(in, "UTF-8");
			System.out.println(resp);
			
			Document doc = XmlHelper.parseString(resp);
			Element fileUrlElem = XmlHelper.element(doc.getDocumentElement(), 
					"fileUrl", true);
			String fileUrl = fileUrlElem != null ? XmlHelper.text(fileUrlElem, true) : null;
			System.out.println(fileUrl);
			
			IoHelper.copy(new URL(fileUrl).openStream(), output);
		} catch (Exception e) {
			throw new RuntimeException("failed to generate audio: " + e, e);
		}
	}

}
