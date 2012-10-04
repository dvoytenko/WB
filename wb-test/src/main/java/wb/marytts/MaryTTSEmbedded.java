package wb.marytts;

import java.io.StringReader;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;

public class MaryTTSEmbedded {

	public static void main(String[] args) throws Exception {

		try {

			MaryInterface marytts = new LocalMaryInterface();

			print("Voices", marytts.getAvailableVoices());
			print("Voices[de]", marytts.getAvailableVoices(new Locale("de")));
			print("Locales", marytts.getAvailableLocales());
			print("Input Types", marytts.getAvailableInputTypes());
			//		print("Output Types", marytts.getAvailableOutputTypes());
			System.out.println("Effects: " + marytts.getAudioEffects());
			
			// See C:\Work\WB\marytts-src\download\marytts-components.xml

			marytts.setVoice(marytts.getAvailableVoices().iterator().next());
			
			/* Audio:
				WAVE_FILE
				AU_FILE
				AIFF_FILE 
			 */
			
			/*
				RAWMARYXML:
					<maryxml version="0.4" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://mary.dfki.de/2002/MaryXML" xml:lang="en-US">
						Welcome<boundary breakindex="4"/>to the world of speech synthesis!
					</maryxml>
					
				INTONATION:
					<maryxml xmlns="http://mary.dfki.de/2002/MaryXML" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="0.4" xml:lang="en-US">
						<p>
							<voice name="cmu-slt">
								<s>
									<phrase>
										<t accent="H*" g2p_method="lexicon" ph="' w E l - k @ m" pos="UH">
											Welcome
										</t>
										<t pos=".">
											!
										</t>
										<boundary breakindex="5" tone="L-L%"/>
									</phrase>
								</s>
							</voice>
						</p>
					</maryxml>
	
				SSML:
					<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis"
  						xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  						xsi:schemaLocation="http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis/synthesis.xsd"
  						xml:lang="en-US">
						Welcome<break/>to the world of speech synthesis!
					</speak>
					
				EMOTIONML: emotionml.xml
	
				APML:
					<!DOCTYPE apml SYSTEM "http://mary.dfki.de/lib/apml.dtd" []>
					<apml xml:lang="en-US">
						<performative  type="announce">
							<theme affect="joy">
								Welcome<boundary type="H"/>to the <emphasis level="strong">wonderful</emphasis> world of
								<emphasis x-pitchaccent="Hstar">speech</emphasis> synthesis
								<boundary type="LL"/>!
							</theme>
						</performative>
					</apml>
	
				SABLE:
					<!DOCTYPE SABLE SYSTEM "http://mary.dfki.de/lib/Sable.v0_2.dtd">
					<SABLE xml:lang="en-US">
						Welcome<BREAK/>to the world of speech synthesis!
					</SABLE>

			 */
			
			AudioInputStream audio = marytts.generateAudio("Hello world. How are you? I'm great!!!");
			
	        LineListener lineListener = new LineListener() {
	            public void update(LineEvent event) {
	                if (event.getType() == LineEvent.Type.START) {
	                    System.err.println("Audio started playing.");
	                } else if (event.getType() == LineEvent.Type.STOP) {
	                    System.err.println("Audio stopped playing.");
	                } else if (event.getType() == LineEvent.Type.OPEN) {
	                    System.err.println("Audio line opened.");
	                } else if (event.getType() == LineEvent.Type.CLOSE) {
	                    System.err.println("Audio line closed.");
	                }
	            }
	        };
			
			AudioPlayer player = new AudioPlayer(audio, lineListener);
			player.start();
			player.join();

			// marytts.setAudioEffects("FIRFilter");
			marytts.setInputType("RAWMARYXML");
			audio = marytts.generateAudio(parseXmlText(
					"<maryxml version=\"0.4\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://mary.dfki.de/2002/MaryXML\" xml:lang=\"en-US\">" +
					"Welcome<boundary duration='2000' breakindex=\"4\"/>to the world of <strong>speech</strong> synthesis!" +
					"</maryxml>"));
			/*
			  boundary: _ @duration \n
			  voice: ;voice name={} gender={} \n
			 */
			player = new AudioPlayer(audio, lineListener);
			player.start();
			player.join();
			
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

		System.exit(0);
	}

	private static Document parseXmlText(String s) throws Exception {
		DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return b.parse(new InputSource(new StringReader(s)));
	}

	private static <T> void print(String name, Set<T> values) {
		System.out.println(name + ":");
		for (T v : values) {
			System.out.println("- " + v);
		}
		System.out.println();
	}

}
