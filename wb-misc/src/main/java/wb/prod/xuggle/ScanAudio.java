package wb.prod.xuggle;

import java.io.File;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class ScanAudio {
	
	public static void main(String[] args) throws Exception {
		
		final File root = new File("C:\\Work\\WB\\work\\recordings\\t2");
		final File file = new File(root, "oldest-story.wav");
		
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
		
		int index = 0;
		
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
            }
            
            System.out.println(index 
            		+ "\t" + samples.getTimeStamp()
            		+ "\t" + samples.getPts()
            		+ "\t" + samples.getNextPts()
            		+ "\t" + packetAudio.getPts()
            		+ "\t" + packetAudio.getDts()
            		);
            
            index++;
		}
		
	}

}
