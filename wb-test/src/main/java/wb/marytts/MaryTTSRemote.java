package wb.marytts;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import marytts.MaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.util.data.audio.AudioPlayer;
import marytts.client.RemoteMaryInterface;

public class MaryTTSRemote {
	
	public static void main(String[] args) {
		
		final boolean play = true;

		try {

			MaryInterface marytts = new RemoteMaryInterface();

			/* Audio:
				WAVE_FILE
				AU_FILE
				AIFF_FILE 
			 */
			
			List<String> voices = new ArrayList<String>(
					Arrays.asList(
							"cmu-bdl-hsmm",
							"cmu-slt",
							"cmu-slt-hsmm",
							"cmu-rms-hsmm",
							"dfki-prudence-hsmm",
							"dfki-spike-hsmm",
							"dfki-obadiah-hsmm",
							"dfki-poppy-hsmm"));
			for (String voice : voices) {
				System.out.println("Voice: " + voice);
				marytts.setVoice(voice);
//				marytts.setStyle(newStyle);
				marytts.setOutputType(MaryDataType.AUDIO.toString());
//				marytts.setOutputTypeParams(params);
				
				/*
				Conversion from audio format PCM_SIGNED 16000.0 Hz, 16 bit, mono, 
				2 bytes/frame, little-endian to requested audio format 
				PCM_SIGNED 48000.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian 
				not supported.
				 */
				
				AudioInputStream audio;
				try {
					audio = marytts.generateAudio("Hello world. How are you? I'm great!!!");
					System.out.println("OK: " + audio.getFormat());
				} catch (Exception e) {
					System.out.println("ERROR: " + e);
					e.printStackTrace();
					continue;
				}
				
				if (!play) {
					AudioSystem.write(audio, AudioFileFormat.Type.WAVE, 
							new File("target/audio1__" + voice + ".wav"));
				} else {
			        LineListener lineListener = new LineListener() {
			            public void update(LineEvent event) {
			            	/*
			                if (event.getType() == LineEvent.Type.START) {
			                    System.err.println("Audio started playing.");
			                } else if (event.getType() == LineEvent.Type.STOP) {
			                    System.err.println("Audio stopped playing.");
			                } else if (event.getType() == LineEvent.Type.OPEN) {
			                    System.err.println("Audio line opened.");
			                } else if (event.getType() == LineEvent.Type.CLOSE) {
			                    System.err.println("Audio line closed.");
			                }
			                */
			            }
			        };
					
					AudioPlayer player = new AudioPlayer(audio, lineListener);
					player.start();
					player.join();
				}
			}

			/*
			// marytts.setAudioEffects("FIRFilter");
			marytts.setInputType("RAWMARYXML");
			audio = marytts.generateAudio(parseXmlText(
					"<maryxml version=\"0.4\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://mary.dfki.de/2002/MaryXML\" xml:lang=\"en-US\">" +
					"Welcome<boundary duration='2000' breakindex=\"4\"/>to the world of <strong>speech</strong> synthesis!" +
					"</maryxml>"));
//			  boundary: _ @duration \n
//			  voice: ;voice name={} gender={} \n

			player = new AudioPlayer(audio, lineListener);
			player.start();
			player.join();
			*/
			
//			System.out.println("text: " + marytts.generateText("Hello world. How are you? :)"));
//			System.out.println("xml: " + marytts.generateXML("Hello world. How are you? :)"));

//			marytts = new LocalMaryInterface();
//			marytts.setLocale(new Locale("en", "GB"));
//			marytts.setVoice(marytts.getAvailableVoices().iterator().next());
//			audio = marytts.generateAudio("Hello world. How are you? :)");
//			player = new AudioPlayer(audio);
//			player.start();
//			player.join();


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
