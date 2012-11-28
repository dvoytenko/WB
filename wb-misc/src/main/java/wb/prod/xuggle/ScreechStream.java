package wb.prod.xuggle;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

import java.io.File;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class ScreechStream {

	private IMediaWriter writer;
	
	private int streamId;

	private int sampleRate;

	private List<IAudioSamples> samplesList = new ArrayList<IAudioSamples>();

	private double baseVelocity;

	private double rate;

	private long offset;

	private Iter iter;

	public ScreechStream(File file, IMediaWriter writer, int streamId, int sampleRate,
			double baseVelocity) {
		this.writer = writer;
		this.streamId = streamId;
		this.sampleRate = sampleRate;
		this.baseVelocity = baseVelocity;
		load(file);
	}

	private void load(File file) {
		IContainer container = IContainer.make();
		
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
		
		IStreamCoder audioCoder = container.getStream(audiostreamt).getStreamCoder();
		if (audioCoder.open(null, null) < 0) {
			throw new RuntimeException("Cant open audio coder");
		}
		
		// audioCoder.getChannels(), audioCoder.getSampleRate()
		System.out.println("- channels: " + audioCoder.getChannels());
		System.out.println("- sample rate: " + audioCoder.getSampleRate());
		
		IPacket packetAudio = IPacket.make();
		
		while ((container.readNextPacket(packetAudio)) >= 0) {
			
			IAudioSamples samples = IAudioSamples.make(512, 
                    audioCoder.getChannels(),
                    IAudioSamples.Format.FMT_S32);
            int offset = 0;
            while (offset < packetAudio.getSize()) {
                int bytesDecodedaudio = audioCoder.decodeAudio(samples, 
                        packetAudio,
                        offset);
                if (bytesDecodedaudio < 0) {
                    throw new RuntimeException("could not detect audio");
                }
                offset += bytesDecodedaudio;
            
                if (samples.isComplete()) {
                	//colSamples.setTimeStamp(this.offset + colSamples.getTimeStamp());
                	samplesList.add(samples);
                	samples = IAudioSamples.make(512, 
		                    audioCoder.getChannels(),
		                    IAudioSamples.Format.FMT_S32);
                }
            }
		}
		
		this.iter = new Iter();
	}
	
	public void update(BoardState state) {
		double rate;
		if (state == null 
				|| state.height == null || state.height > 0.0 
				|| state.velocity == null || Math.abs(state.velocity) < 1e-3) {
			rate = 0.0;
		} else {
			rate = state.velocity/this.baseVelocity;
			rate = Math.max(Math.min(rate, 3.8), 0.7);
			// console.log('soundfx: rate: ' + rate);
		}
		
		if (this.rate != rate) {
			this.rate = rate;
			System.out.println("soundfx rate: " + rate);
		}
	}
	
	public void setOffset(long offset) {
		this.offset = offset;
	}

	public void advanceTo(long frameTime) {
		
		if (this.offset >= frameTime) {
			return;
		}
		
		final long start = this.offset;
		int sampNum = (int) IAudioSamples.defaultPtsToSamples(frameTime - start, sampleRate);
		short[] samples = new short[sampNum];
		resample(samples);
		writer.encodeAudio(streamId, samples, this.offset, DEFAULT_TIME_UNIT);

		this.offset = frameTime;
	}

	private void resample(short[] samples) {
		if (this.rate < 1e-3) {
			// == 0
			// do nothing: leave samples as 0
		} else if (Math.abs(this.rate - 1) < 1e-3) {
			// == 1
			// push samples as is 1:1
			iter.get(samples);
		} else if (this.rate < 1.0) {
			// < 1
			// play slower 1:n
			// final int n = (int) Math.round(1 / this.rate);
			final int n = 2;
			for (int i = 0; i < samples.length; i += n) {
				short k = iter.get();
				for (int j = 0; j < n; j++) {
					samples[i + j] = k;
				}
			}
		} else if (this.rate > 1.0) {
			// > 1
			// play faster: n:1
			final int n = 2;
			for (int i = 0; i < samples.length; i++) {
				short k = iter.get();
				for (int j = 0; j < n - 1; j++) {
					iter.get();
				}
				samples[i] = k;
			}
		}
	}
	
	private class Iter {
		
		private int index = 0;
		
		private ShortBuffer buffer;
		
		public Iter() {
		}

		public short get() {
			while (buffer == null || buffer.remaining() == 0) {
				index = (index + 1) % samplesList.size();
				buffer = samplesList.get(index).getByteBuffer().asShortBuffer();
				buffer.rewind();
			}
			return buffer.get();
		}
		
		public void get(short[] samples) {
			for (int i = 0; i < samples.length; i++) {
				samples[i] = get();
			}
		}

	}
	
}
