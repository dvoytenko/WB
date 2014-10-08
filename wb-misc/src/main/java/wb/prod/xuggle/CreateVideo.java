package wb.prod.xuggle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;


import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

public class CreateVideo {
	
	/*
	 * oldest-story: 		0001
	 * whole-world-using: 	0114
	 * invite:				0332
	 * dashing-door: 		0573
	 * falling-asleep:		0650
	 * dynamic-pres: 		0727
	 * engaged:				0935
	 * dance:				0996
	 * express-yourself:	1189
	 */
	
	public static void main(String[] args) throws Exception {
		
		final File root = new File("/Users/dvoytenko/work/WB/work/recordings/1412785335299-903");
		final File target = new File(root, "z1.mp4");
		
		final boolean printFrameIndex = false;
		final boolean saveConvImages = false;
		
		final int width = 720;
		final int height = 404;
		
		ScriptStream script = new ScriptStream(root); 
		
		IMediaWriter writer = ToolFactory.makeWriter(target.toString());
		
		writer.addVideoStream(0, 0, width, height);

		/*
		final int screechId = 1;
		final int screechRate = 22050;
		writer.addAudioStream(screechId, 0, 1, screechRate);
		*/
		
		final int speechId = 1;
		final int speechRate = 22050;
		writer.addAudioStream(speechId, 0, 1, speechRate);
		
		writer.setForceInterleave(true);
		
		final long frameRate = DEFAULT_TIME_UNIT.convert(1000L / 30L, 
				TimeUnit.MILLISECONDS);
		long frameTime = 0;

		/*
		ScreechStream screechStream = new ScreechStream(new File(root, "wb-sounds-22050.wav"), 
				writer, screechId, screechRate, 700.0);
		screechStream.setOffset(frameRate);
		*/
		
		SpeechStream speechStream = new SpeechStream(root, writer, speechId, speechRate,
				new ScreechSubStream(root, 500.0));
		
//		boolean started = false;
		Fragment fragment;
		BufferedImage frame = null;
		while ((fragment = script.next()) != null) {
//			if (fileName.equals("0000000001.png")) {
//				started = true;
//			}
//			if (!started) {
//				continue;
//			}
//			if (fileName.equals("0000000013.png")) {
//				nextFrameTime += frameRate * 300L;
//				continue;
//			}
//			if (fileName.equals("0000000700.png")) {
//				break;
//			}
			
//			long prevFrameTime = frameTime;
			frameTime += frameRate;

			System.out.println("fragment: " + fragment.name + " (" + frameTime + ")");
			
			if (fragment.speech != null) {
				
				// blank
				while (!speechStream.isBlank()) {
					speechStream.advanceTo(frameTime);
					frameTime += frameRate;
				}
				
				// switch to the next speech segment
				speechStream.open(fragment.speech, frameTime);
			}

			BufferedImage image = ImageIO.read(new File(root, fragment.name + ".png"));
			if (image.getWidth() != width || image.getHeight() != height) {
				throw new RuntimeException("invalid width/height");
			}
			frame = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
			if (printFrameIndex) {
				addFrameNum(frame, fragment.name);
			}
			if (saveConvImages) {
				ImageIO.write(image, "png", 
						new File(root, fragment.name + ".conv.png"));
			}
			writer.encodeVideo(0, frame, frameTime, DEFAULT_TIME_UNIT);
			
			speechStream.update(fragment.boardState);
			speechStream.advanceTo(frameTime);
			
			/*
			screechStream.update(fragment.boardState);
			screechStream.advanceTo(frameTime);
			*/
		}

		// catch up speech segments
		while (!speechStream.isBlank()) {
			frameTime += frameRate;
			if (frame != null) {
				writer.encodeVideo(0, frame, frameTime, DEFAULT_TIME_UNIT);
			}
			speechStream.advanceTo(frameTime);
			/*
			screechStream.advanceTo(frameTime);
			*/
		}
		
		writer.close();
	}

	private static void addFrameNum(BufferedImage image, String frameStr) throws IOException {
		Graphics g = image.getGraphics();
		// g.setColor(Color.black);
		g.setColor(new Color(0, 0, 0, 20));
		g.setFont(g.getFont().deriveFont(20f));
		g.drawString(frameStr, 10, 30);
	}

	public static BufferedImage convertToType(BufferedImage sourceImage,
			int targetType) {
		
		BufferedImage image;

		// if the source image is already the target type, return the source image

		if (sourceImage.getType() == targetType) {
			image = sourceImage;
		} else {
			// otherwise create a new image of the target type and draw the new
			// image
			image = new BufferedImage(sourceImage.getWidth(),
					sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}

		return image;
	}
	
}
