package wb.prod.xuggle;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class SpeechStream {

	private File root;

	private IMediaWriter writer;

	private IPacket packetAudio;

	private long offset;

	private IContainer container;

	private IStreamCoder audioCoder;

	private final int sampleRate;

	private final Queue<IAudioSamples> samplesQueue = new LinkedList<IAudioSamples>();

	private int streamId;
	
	private final boolean actualEncode = true;

	public SpeechStream(File root, IMediaWriter writer, int streamId, int sampleRate) {
		this.root = root;
		this.writer = writer;
		this.streamId = streamId;
		this.sampleRate = sampleRate;
		this.packetAudio = IPacket.make();
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
			if (actualEncode) {
				writer.encodeAudio(streamId, samples);
			}
		}

		if (!samplesQueue.isEmpty() && container != null) {
			return;
		}

		// TODO somehow first frame is always skipped here... is it right? 
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
					if (actualEncode) {
						writer.encodeAudio(streamId, samples);
					}
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
			long totalSampleCount = 0L;
			long sampNum = IAudioSamples.defaultPtsToSamples(frameTime - start, sampleRate);
			short[] samples = null;
			while (totalSampleCount < sampNum) {
				int sampNum0 = (int) Math.min(sampNum - totalSampleCount, 1000L);
				if (samples == null || samples.length != sampNum0) {
					samples = new short[sampNum0];
				}
				if (actualEncode) {
					writer.encodeAudio(streamId, samples, 
							start + IAudioSamples.samplesToDefaultPts(totalSampleCount, sampleRate), 
							DEFAULT_TIME_UNIT);
				}
				totalSampleCount += sampNum0;
			}

			this.offset = frameTime;

			System.out.println("b: " + start + "/" + frameTime);
		}
	}

}
