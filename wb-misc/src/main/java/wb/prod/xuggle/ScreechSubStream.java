package wb.prod.xuggle;

import java.io.File;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class ScreechSubStream {

	private double baseVelocity;

	private double rate;

	private Iter iter100;

	private Iter iter075;

	private Iter iter150;

	private Iter iter200;

	public ScreechSubStream(File root, double baseVelocity) {
		this.baseVelocity = baseVelocity;
		this.iter100 = load(new File(root, "wb-sounds-22050.wav"));
		this.iter075 = load(new File(root, "wb-sounds-22050-075.wav"));
		this.iter150 = load(new File(root, "wb-sounds-22050-150.wav"));
		this.iter200 = load(new File(root, "wb-sounds-22050-200.wav"));
	}

	private static Iter load(File file) {
		
		List<IAudioSamples> samplesList = new ArrayList<IAudioSamples>();
		
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
		
		return new Iter(samplesList);
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
	
	public short[] samples(int sampNum) {
		short[] samples = new short[sampNum];
		resample(samples);
		return samples;
	}

	private void resample(short[] samples) {
		Iter iter;
		if (this.rate < 1e-3) {
			// == 0
			// do nothing: leave samples as 0
			iter = null;
		} else if (Math.abs(this.rate - 1) < 1e-3) {
			// == 1
			// push samples as is 1:1
			iter = this.iter100;
		} else if (this.rate < 1.0) {
			// < 1
			iter = this.iter075;
		} else if (this.rate >= 2.0) {
			// >= 2
			iter = this.iter200;
		} else if (this.rate > 1.0) {
			// > 1
			// play faster: n:1
			iter = this.iter150;
		} else {
			iter = this.iter100;
		}

		if (iter != null) {
			iter.get(samples);
		}
	}
	
	private static class Iter {
		
		private final List<IAudioSamples> samplesList;
		
		private int index = 0;
		
		private ShortBuffer buffer;

		public Iter(List<IAudioSamples> samplesList) {
			this.samplesList = samplesList;
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
