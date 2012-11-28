package wb.prod.xuggle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class ScreecherAudio2 {
	
	public static void main(String[] args) throws Exception {
		
		final File root = new File("C:\\Work\\WB\\work\\recordings\\t2");
		final File file = new File(root, "wb-sounds-16000.wav");
		
		final File destinationFile = new File(root, "x4.wav");
		
		IMediaWriter writer = ToolFactory.makeWriter(destinationFile.toString());
		writer.addAudioStream(0, 0, 1, 16000);
		
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
		
		List<IAudioSamples> samplesList = new ArrayList<IAudioSamples>();
		
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
		
		System.out.println(samplesList.size());
		
		/*
		// at
		int index = 0;
		for (IAudioSamples samples : samplesList) {
			int len = (int) samples.getNumSamples();
			System.out.println("encode " + index + ": " + len);
			writer.encodeAudio(0, samples);
			index++;
		}
		*/

		/*
		// faster
		int index = 0;
		for (IAudioSamples samples : samplesList) {
			int len = (int) samples.getNumSamples();
			System.out.println("encode " + index + ": " + len);
			short[] array = new short[len * 2];
			samples.getByteBuffer().asShortBuffer().get(array, 0, len);
			writer.encodeAudio(0, array);
			index++;
		}
		*/

		/*
		// faster 2
		int index = 0;
		for (IAudioSamples samples : samplesList) {
			int len = (int) samples.getNumSamples();
			System.out.println("encode " + index + ": " + len);
			short[] array = new short[len / 2];
			samples.getByteBuffer().asShortBuffer().get(array, 0, len / 2);
			writer.encodeAudio(0, array);
			index++;
		}
		*/

		// slower
		int index = 0;
		for (IAudioSamples samples : samplesList) {
			//IAudioSamples.make(buffer, channels, Format.FMT_);
			int numSamples = (int) samples.getNumSamples();
			System.out.println("encode " + index + ": " + numSamples);
			short[] array = new short[numSamples];
			short[] array2 = new short[numSamples * 2];
			samples.getByteBuffer().asShortBuffer().get(array, 0, numSamples);
			for (int i = 0; i < numSamples; i++) {
				array2[2 * i] = array[i];
				array2[2 * i + 1] = array[i];
			}
			writer.encodeAudio(0, array2);
			index++;
		}
		
		writer.close();
	}

}
