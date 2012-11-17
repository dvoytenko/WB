package wb.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class PrepareScript {

	private File outputFolder;
	
	private File shapesFolder;

	private File imagesFolder;

	@SuppressWarnings("unused")
	private File soundsFolder;

	private TtsEngines ttsEngines = new TtsEngines();

	private File fontsFolder;
	
	private Map<String, Font> fonts = new HashMap<String, Font>();
	
	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setShapesFolder(File shapesFolder) {
		this.shapesFolder = shapesFolder;
	}
	
	public void setImagesFolder(File imagesFolder) {
		this.imagesFolder = imagesFolder;
	}

	public void setSoundsFolder(File soundsFolder) {
		this.soundsFolder = soundsFolder;
	}

	public void setFontsFolder(File fontsFolder) {
		this.fontsFolder = fontsFolder;
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
			Shape shape = new Parser().fromJsonFile(shapeFile, GroupShape.class);
			return shape;
		} catch (Exception e) {
			throw new RuntimeException("failed to load shape [" + shapeId + "]: " + e, e);
		}
	}
	
	public ImageShape getImageShape(String imageId) {
		
		if (!imageId.contains(".")) {
			imageId += ".png";
		}
		
		File imageFile = new File(this.imagesFolder, imageId);

		try {
			ImageShape shape = new ImageShape();
			
			BufferedImage image = ImageIO.read(imageFile);
			// image.imageUrl = imageId;

//			StringWriter w = new StringWriter();
//			w.write("data:image/png;base64,");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] bytes = baos.toByteArray();
			String data = StringUtils.newStringUtf8(Base64.encodeBase64(bytes, false));
//			w.write(data);
			
			shape.data = data;
			
			shape.width = (double) image.getWidth();
			shape.height = (double) image.getHeight();
			
			return shape;
		} catch (Exception e) {
			throw new RuntimeException("failed to load image [" + imageId + "]: " + e, e);
		}
	}

	public Font getFont(String fontName) {
		Font font = this.fonts.get(fontName);
		if (font == null) {
			font = loadFont(fontName);
			this.fonts.put(fontName, font);
		}
		return font;
	}

	private Font loadFont(String fontName) {
		try {
			return new Parser().fromJsonFile(new File(this.fontsFolder, 
					fontName + ".json"), Font.class);
		} catch (Exception e) {
			throw new RuntimeException("can't get font [" + fontName + "]: " + e, e);
		}
	}

}
