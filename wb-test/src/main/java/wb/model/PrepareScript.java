package wb.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wb.util.IoHelper;
import wb.util.XmlHelper;

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
						if (voice != null) {
							Element voiceEl = request.createElement("voice");
							root.appendChild(voiceEl);
							voiceEl.appendChild(request.createTextNode(voice));
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
				
				IoHelper.readFile(new URL(fileUrl), file);
			} catch (Exception e) {
				throw new RuntimeException("failed to generate audio: " + e, e);
			}
		}
		
	}
	
}
