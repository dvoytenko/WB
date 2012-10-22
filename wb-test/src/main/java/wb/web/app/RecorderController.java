package wb.web.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import wb.util.IoHelper;

@Controller
@RequestMapping( { "/recorder" })
public class RecorderController {
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value="/start")
	@ResponseBody
	public Recording start() {
		Recording recording = new Recording();
		recording.id = "" + System.currentTimeMillis() + "-" + 
				String.valueOf(Math.round(Math.random() * 1000));
		File writerDir = new File("target/" + recording.id);
		if (writerDir.exists()) {
			throw new RuntimeException("directory already exists: " + writerDir);
		}
		if (!writerDir.mkdir()) {
			throw new RuntimeException("failed to create dir: " + writerDir);
		}
		return recording;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="/frame")
	@ResponseBody
	public String frame(@RequestParam("recordId") String recordId, 
			@RequestParam("frameIndex") int frameIndex,
			Reader reader) throws Exception {
		File writerDir = new File("target/" + recordId);
		if (!writerDir.exists()) {
			throw new RuntimeException("directory doesn't exists: " + writerDir);
		}
		
		String frameId = frameId(frameIndex);
		System.out.println("frame: " + frameId);
		
		File frameFile = new File(writerDir, frameId + ".json");
		if (frameFile.exists()) {
			throw new RuntimeException("frame already exists: " + frameIndex);
		}
		
		/*
			state: board._state,
			speech: speechPlayer.playingUrl,
			width: canvasWidth,
			height: canvasHeight,
			commit: board.commitPane.canvas.toDataURL(),
			anim: board.animationPane.canvas.toDataURL(),
			pointer: board.pointer.pane.canvas.toDataURL(),
		 */
		
		JSONObject inp = new JSONObject(new JSONTokener(reader));
		
		JSONObject outp = new JSONObject();
		for (Iterator<String> i = keys(inp); i.hasNext();) {
			String key = i.next();
			if ("panes".equals(key)) {
				continue;
			}
			outp.put(key, inp.get(key));
		}
		IoHelper.writeText(outp.toString(2), frameFile, "UTF-8", "\n");
		
		int width = inp.getInt("width")/2*2;
		int height = inp.getInt("height")/2*2;
		
		JSONArray panes = inp.getJSONArray("panes");
		String[] imageDataList = new String[panes.length()];
		for (int i = 0; i < panes.length(); i++) {
			imageDataList[i] = panes.getJSONObject(i).getString("data");
		}
		
		BufferedImage image = convCombineImages(width, height,
				imageDataList);
		
		ImageIO.write(image, "png", new File(writerDir, frameId + ".png"));
		ImageIO.write(image, "jpg", new File(writerDir, frameId + ".jpg"));
		
		return "ok";
	}
	
	@SuppressWarnings("unchecked")
	private Iterator<String> keys(JSONObject inp) {
		return inp.keys();
	}

	private BufferedImage convCombineImages(int width, int height,
			String[] imageDataList) throws IOException {
		
		BufferedImage all = new BufferedImage(width, height, 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = all.createGraphics();
		
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		for (String imageData : imageDataList) {
			BufferedImage image = convImage(imageData);
			g.drawImage(image, null, 0, 0);
		}
		
		return all;
	}

	private static BufferedImage convImage(String imageData) throws IOException {
		// 012345678901234567890
		// data:image/png;base64,
		final String prefix = "data:image/png;base64,";
		if (!imageData.startsWith(prefix)) {
			throw new IllegalArgumentException("unknown image data: " + imageData.substring(0, 50));
		}
		String data = imageData.substring(prefix.length());
		byte[] bytes = Base64.decodeBase64(data);
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}
	
	private String frameId(int frameIndex) {
		StringBuilder sb = new StringBuilder();
		sb.append(frameIndex);
		while (sb.length() < 10) {
			sb.insert(0, "0");
		}
		return sb.toString();
	}

	public static class Recording {
		
		public String id;
		
	}
	
}
