package wb.prod.xuggle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.json.JSONException;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

public class CreateVideoNoAudio {
	
	public static void main(String[] args) throws Exception {
		
		final File root = new File("C:\\Work\\WB\\work\\recordings\\t2");
		final File target = new File(root, "z2.mp4");
		
		final boolean printFrameIndex = true;
		final boolean saveConvImages = false;
		
		final int width = 720;
		final int height = 404;
		
		ScriptStream script = new ScriptStream(root); 
		
		IMediaWriter writer = ToolFactory.makeWriter(target.toString());
		
		writer.addVideoStream(0, 0, width, height);
		
		writer.addAudioStream(1, 0, 1, 22050);
		
		writer.setForceInterleave(true);
		
		final long frameRate = DEFAULT_TIME_UNIT.convert(1000L / 30L, 
				TimeUnit.MILLISECONDS);
		long frameTime = 0;
		
//		boolean started = false;
		Fragment fragment;
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
			
			BufferedImage image = ImageIO.read(new File(root, fragment.name + ".png"));
			if (image.getWidth() != width || image.getHeight() != height) {
				throw new RuntimeException("invalid width/height");
			}
			BufferedImage frame = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
			if (printFrameIndex) {
				addFrameNum(frame, fragment.name);
			}
			if (saveConvImages) {
				ImageIO.write(image, "png", 
						new File(root, fragment.name + ".conv.png"));
			}
			writer.encodeVideo(0, frame, frameTime, DEFAULT_TIME_UNIT);
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
	
	public static class ScriptStream {
		
		private File root;
		
		private String[] fragments;
		
		private int pointer = -1;

		public ScriptStream(File root) {
			this.root = root;

			String[] fragments = root.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("0") && name.endsWith(".json");
				}
			});
			Arrays.sort(fragments);
			this.fragments = fragments;
		}
		
		public int getFragmentCount() {
			return this.fragments.length;
		}
		
		public Fragment next() throws IOException, JSONException {
			this.pointer++;
			if (this.pointer >= this.fragments.length) {
				return null;
			}
			return new Fragment(new File(this.root, this.fragments[this.pointer]));
		}
		
	}
	
	public static class Fragment {
		
		public final String name;

		public final int index;
		
		public Fragment(File file) throws IOException, JSONException {
			
			this.name = file.getName().replace(".json", "");
			this.index = Integer.parseInt(this.name);
		}
		
	}
	
}
