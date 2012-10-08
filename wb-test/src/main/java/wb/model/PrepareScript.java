package wb.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.MaryInterface;
import marytts.client.RemoteMaryInterface;
import marytts.datatypes.MaryDataType;

import org.json.JSONException;
import org.json.JSONObject;

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

	public String createAudio(String text, String voice) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		
		if (voice == null) {
			voice = "cmu-slt-hsmm";
//			voice = "dfki-prudence-hsmm";
		}

		String track;
		try {
			track = UUID.nameUUIDFromBytes((voice + text).getBytes("UTF-8")).
					toString().toLowerCase();
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}
		
		if (new File(this.outputFolder, track + ".wav").exists()) {
			return track;
		}
		
		try {
			MaryInterface marytts = new RemoteMaryInterface();
			marytts.setVoice(voice);
			marytts.setOutputType(MaryDataType.AUDIO.toString());
			
			AudioInputStream audio = marytts.generateAudio(text);
			
			AudioSystem.write(audio, AudioFileFormat.Type.WAVE, 
					new File(this.outputFolder, track + ".wav"));
			
			return track;
		} catch (Exception e) {
			throw new RuntimeException("failed to generate audio: " + e, e);
		}
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
