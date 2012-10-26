package wb.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import wb.util.IoHelper;

public class PrepareScript {

	private File outputFolder;
	
	private File shapesFolder;

	@SuppressWarnings("unused")
	private File soundsFolder;

	private TtsEngines ttsEngines = new TtsEngines();
	
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
		
		TtsEngine engine = ttsEngines.getEngine(engineCode);
		if (engine == null) {
			throw new RuntimeException("unknown engine: " + engineCode);
		}
		
		if (voice == null) {
			voice = engine.getDefaultVoiceId();
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
		System.out.println("Track file: " + file);
		if (file.exists()) {
			return track;
		}
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			engine.generateAudio(text, voice, out);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("failed to generate audio [" + file + "]: " + e, e);
		}
		
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

}
