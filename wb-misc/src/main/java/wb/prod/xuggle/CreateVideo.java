package wb.prod.xuggle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

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
		
		final File root = new File("C:\\Work\\WB\\work\\recordings\\t2");
		final File target = new File(root, "z1.mp4");
		
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
		
		SpeechProcessor speechProcessor = new SpeechProcessor(root, writer, 22050);
		
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
				while (!speechProcessor.isBlank()) {
					speechProcessor.advanceTo(frameTime);
					frameTime += frameRate;
				}
				
				// switch to the next speech segment
				speechProcessor.open(fragment.speech, frameTime);
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
			
			speechProcessor.advanceTo(frameTime);
		}

		// catch up speech segments
		while (!speechProcessor.isBlank()) {
			frameTime += frameRate;
			if (frame != null) {
				writer.encodeVideo(0, frame, frameTime, DEFAULT_TIME_UNIT);
			}
			speechProcessor.advanceTo(frameTime);
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
	
	public static class SpeechProcessor {

		private File root;
		
		private IMediaWriter writer;

		private IPacket packetAudio;
		
		private long offset;

		private IContainer container;

		private IStreamCoder audioCoder;

		private final int sampleRate;
		
		private final Queue<IAudioSamples> samplesQueue = new LinkedList<IAudioSamples>(); 

		public SpeechProcessor(File root, IMediaWriter writer, int sampleRate) {
			this.root = root;
			this.writer = writer;
			this.packetAudio = IPacket.make();
			this.sampleRate = sampleRate;
		}

		public boolean isBlank() {
			return this.container == null;
		}
		
		public void open(String speech, long startTime) {
			
			System.out.println("speech: " + speech);
			this.offset = startTime;
			
//			this.reader = ToolFactory.makeReader(new File(root, speech).getPath());
			container = IContainer.make();
			
			File file = new File(this.root, speech);
			
			if (container.open(file.getPath(), IContainer.Type.READ, null) < 0) {
		        throw new IllegalArgumentException("Cant find " + file);
			}
			
			int audiostreamt = -1;
			for (int i = 0; i < container.getNumStreams(); i++) {
				IStream stream = container.getStream(i);
				IStreamCoder code = stream.getStreamCoder();
				if (code.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
					audiostreamt = i;
					break;
				}
			}
			if (audiostreamt == -1) {
				throw new RuntimeException("No audio steam found");			
			}
			
			audioCoder = container.getStream(audiostreamt).getStreamCoder();
			if (audioCoder.open(null, null) < 0) {
				throw new RuntimeException("Cant open audio coder");
			}
			
			// audioCoder.getChannels(), audioCoder.getSampleRate()
			System.out.println("- channels: " + audioCoder.getChannels());
			System.out.println("- sample rate: " + audioCoder.getSampleRate());
			if (audioCoder.getChannels() != 1) {
				throw new IllegalArgumentException("number of channels doesn't match");
			}
			if (audioCoder.getSampleRate() != sampleRate) {
				throw new IllegalArgumentException("sample rate doesn't match");
			}
		}

		public void advanceTo(long frameTime) {
			
            while (!samplesQueue.isEmpty() 
            		&& samplesQueue.peek().getTimeStamp() < frameTime) {
            	IAudioSamples samples = samplesQueue.poll();
            	System.out.println("a: -> " + samples.getTimeStamp() 
            			+ " to " + samples.getNextPts());
            	this.writer.encodeAudio(1, samples);
            }
            
            if (!samplesQueue.isEmpty() && container != null) {
            	return;
            }
			
			int TODO1; // somehow first frame is always skipped here... is it right? 
			if (this.offset >= frameTime) {
				return;
			}
			
			if (container != null) {
				int read;
				while ((read = container.readNextPacket(packetAudio)) >= 0) {
					
					IAudioSamples colSamples = IAudioSamples.make(512, 
		                    audioCoder.getChannels(),
		                    IAudioSamples.Format.FMT_S32);
		            int offset = 0;
		            while (offset < packetAudio.getSize()) {
		                int bytesDecodedaudio = audioCoder.decodeAudio(colSamples, 
		                        packetAudio,
		                        offset);
		                if (bytesDecodedaudio < 0) {
		                    throw new RuntimeException("could not detect audio");
		                }
		                offset += bytesDecodedaudio;
		                
		                if (colSamples.isComplete()) {
		                	colSamples.setTimeStamp(this.offset + colSamples.getTimeStamp());
		                	samplesQueue.offer(colSamples);
		                	colSamples = IAudioSamples.make(512, 
				                    audioCoder.getChannels(),
				                    IAudioSamples.Format.FMT_S32);
		                }
		            }
		            
		            while (!samplesQueue.isEmpty() 
		            		&& samplesQueue.peek().getTimeStamp() < frameTime) {
		            	IAudioSamples samples = samplesQueue.poll();
		            	System.out.println("a: -> " + samples.getTimeStamp() 
		            			+ " to " + samples.getNextPts());
	                	this.writer.encodeAudio(1, samples);
		            }

		            if (!samplesQueue.isEmpty() 
		            		&& samplesQueue.peek().getTimeStamp() >= frameTime) {
		            	break;
		            }
				}
				if (read < 0) {
					this.container = null;
					this.offset = frameTime;
                	System.out.println("a: complete");
				}
			} else {
				
				final long start = this.offset;
				/*
				long clock = start;
				for (; clock < frameTime; clock = start 
						+ IAudioSamples.samplesToDefaultPts(totalSampleCount, sampleRate)) {
					short[] samples = new short[1000];
					writer.encodeAudio(1, samples, clock, DEFAULT_TIME_UNIT);
					totalSampleCount += samples.length;
				}
				*/
				long totalSampleCount = 0L;
				long sampNum = IAudioSamples.defaultPtsToSamples(frameTime - start, sampleRate);
				short[] samples = null;
				while (totalSampleCount < sampNum) {
					int sampNum0 = (int) Math.min(sampNum - totalSampleCount, 1000L);
					if (samples == null || samples.length != sampNum0) {
						samples = new short[sampNum0];
					}
					writer.encodeAudio(1, samples, 
							start + IAudioSamples.samplesToDefaultPts(totalSampleCount, sampleRate), 
							DEFAULT_TIME_UNIT);
					totalSampleCount += sampNum0;
				}
				
				this.offset = frameTime;

            	System.out.println("b: " + start + "/" + frameTime);
			}
		}

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
		
		public String speech;

		public Fragment(File file) throws IOException, JSONException {
			
			this.name = file.getName().replace(".json", "");
			this.index = Integer.parseInt(this.name);
			
			JSONObject js;
			Reader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			try {
				js = new JSONObject(new JSONTokener(reader));
			} finally {
				reader.close();
			}
			
			this.speech = js.optString("speech");
			if ("null".equals(this.speech) || "".equals(this.speech)) {
				this.speech = null;
			}
			if (this.speech != null) {
				this.speech = this.speech.substring(this.speech.lastIndexOf('/') + 1);
			}
		}
		
	}
	
}
