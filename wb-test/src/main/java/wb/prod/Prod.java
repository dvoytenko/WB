package wb.prod;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import wb.util.IoHelper;

public class Prod {
	
	// ffmpeg.exe -r 30 -b:v 1800 -i ^%010d.png -probesize 100 -analyzeduration 10000000 r30b1800.mp4
	
	public static void main(String[] args) throws Exception {
		final String frame = "1000";
		File dir = new File("C:\\Work\\WB\\wb-test\\target\\1350788300811-278");
		File f = new File(dir, frame + ".json");
		String s = IoHelper.readText(f);
		JSONObject js = new JSONObject(s);
		
		// commitPane -> commit
		// animationPane -> anim
		// pointerPane -> pointer
		
		String commitPane = js.getString("commitPane");
		saveImage(commitPane, new File(dir, frame + "-c.png"));

		String animPane = js.getString("animationPane");
		saveImage(animPane, new File(dir, frame + "-a.png"));
		
		combineAndSave(new String[]{commitPane, animPane}, 
				new File(dir, frame + "-all.png"));
	}

	private static void combineAndSave(String[] imageDataList, File file) throws IOException {
		
		// TODO make sure that anti-aliasing is off
		BufferedImage all = null;
		Graphics2D g = null;
		
		for (String imageData : imageDataList) {
			BufferedImage image = convImage(imageData);
			if (g == null) {
				all = new BufferedImage(image.getWidth(), image.getHeight(), 
						BufferedImage.TYPE_INT_ARGB);
				g = all.createGraphics();
			}
			g.drawImage(image, null, 0, 0);
		}
		
		ImageIO.write(all, "png", file);
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
	
	private static void saveImage(String imageData, File file) throws IOException {
		ImageIO.write(convImage(imageData), "png", file);
	}

}
