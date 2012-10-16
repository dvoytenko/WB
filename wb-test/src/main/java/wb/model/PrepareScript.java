package wb.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.MaryInterface;
import marytts.client.RemoteMaryInterface;

import org.dom4j.Document;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import wb.util.IoHelper;

public class PrepareScript {

	private File outputFolder;
	
	private File shapesFolder;

	@SuppressWarnings("unused")
	private File soundsFolder;

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setShapesFolder(File shapesFolder) {
		this.shapesFolder = shapesFolder;
	}

	public void setSoundsFolder(File soundsFolder) {
		this.soundsFolder = soundsFolder;
	}

	public void saveObject(String name, Object object) throws JSONException, IOException {
		
		JSONObject js = (JSONObject) new Serializer().toJson(object);
		File targetFile = new File(this.outputFolder, name + ".json");
		
		Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
		writer.write(js.toString(2));
		writer.close();
	}

	public String createAudio(String text, String engineCode, String voice) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		
		if (engineCode == null) {
			engineCode = "mary";
		}
		
		SpeechEngine engine;
		if ("mary".equals(engineCode)) {
			engine = new MaryEngine();
		} else if ("cere".equals(engineCode)) {
			engine = new CereEngine();
		} else {
			throw new RuntimeException("unknown engine: " + engineCode);
		}
		
		if (voice == null) {
			voice = engine.getDefaultVoice();
		}
		
		String track;
		try {
			track = engineCode + "-" + 
					UUID.nameUUIDFromBytes((voice + text).getBytes("UTF-8")).
						toString().toLowerCase();
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}
		
		final File file = new File(this.outputFolder, track + ".wav");
		System.out.println("Track file: " + track);
		if (file.exists()) {
			return track;
		}
		
		engine.generateAudio(text, voice, file);
		
		return track;
	}

	public Shape getShape(String shapeId) {
		
		if (!shapeId.endsWith(".json")) {
			shapeId += ".json";
		}
		
		File shapeFile = new File(this.shapesFolder, shapeId);
		
		try {
			JSONObject js = new JSONObject(IoHelper.readText(shapeFile, "UTF-8"));
			Shape shape = (Shape) new Parser().fromJson(js);
			return shape;
		} catch (Exception e) {
			throw new RuntimeException("failed to load shape [" + shapeId + "]: " + e, e);
		}
	}

	private static interface SpeechEngine {

		String getDefaultVoice();

		void generateAudio(String text, String voice, File file);
		
	}
	
	private static class MaryEngine implements SpeechEngine {

		@Override
		public String getDefaultVoice() {
			return "cmu-slt-hsmm";
		}

		@Override
		public void generateAudio(String text, String voice, File file) {
			try {
				MaryInterface marytts = new RemoteMaryInterface();
				marytts.setVoice(voice);
				marytts.setOutputType("AUDIO");
				
				AudioInputStream audio = marytts.generateAudio(text);
				
				AudioSystem.write(audio, AudioFileFormat.Type.WAVE, 
						file);
			} catch (Exception e) {
				throw new RuntimeException("failed to generate audio: " + e, e);
			}
		}
		
	}
	
	private static class CereEngine implements SpeechEngine {

		@Override
		public String getDefaultVoice() {
			return "Isabella";
		}

		@Override
		public void generateAudio(String text, String voice, File file) {
			try {
				
				DOMDocument request = new DOMDocument();
				{
					DOMElement root = new DOMElement("speakExtended");
					request.add(root);
					{
						// accountID
						DOMElement accountId = new DOMElement("accountID");
						root.add(accountId);
						accountId.setText("50727aae31c52");
						
						// password
						DOMElement pass = new DOMElement("password");
						root.add(pass);
						pass.setText("S56HyCi3mw");
						
						// voice
						if (voice != null) {
							DOMElement voiceEl = new DOMElement("voice");
							root.add(voiceEl);
							voiceEl.setText(voice);
						}
						
						// audioFormat
						DOMElement formatEl = new DOMElement("audioFormat");
						root.add(formatEl);
						formatEl.setText("wav");
						
						// text
						DOMElement textEl = new DOMElement("text");
						root.add(textEl);
						textEl.setText(text);
					}
				}
				
				StringWriter stringWriter = new StringWriter();
				XMLWriter xmlWriter = new XMLWriter(stringWriter);
				xmlWriter.write(request);
				
				String requestXml = stringWriter.toString();
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
				
				InputSource source = new InputSource(new StringReader(resp));
				SAXReader reader = new SAXReader();
				Document doc = reader.read(source);
				String fileUrl = doc.getRootElement().element("fileUrl").getText();
				System.out.println(fileUrl);
				
				IoHelper.readFile(new URL(fileUrl), file);
			} catch (Exception e) {
				throw new RuntimeException("failed to generate audio: " + e, e);
			}
		}
		
	}
	
}
