package wb.marytts;

import java.util.Locale;
import java.util.Set;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;

public class MaryTTSInspect {

	public static void main(String[] args) throws Exception {

		MaryInterface marytts = new LocalMaryInterface();

		print("Voices", marytts.getAvailableVoices());
		print("Voices[en]", marytts.getAvailableVoices(new Locale("en")));
		print("Voices[en_US]", marytts.getAvailableVoices(new Locale("en", "US")));
		print("Voices[de]", marytts.getAvailableVoices(new Locale("de")));
		print("Voices[ru]", marytts.getAvailableVoices(new Locale("ru")));

		print("Locales", marytts.getAvailableLocales());
		print("Input Types", marytts.getAvailableInputTypes());
		print("Output Types", marytts.getAvailableOutputTypes());

		System.out.println("Effects: " + marytts.getAudioEffects());
			
		System.exit(0);
	}

	private static <T> void print(String name, Set<T> values) {
		System.out.println(name + ":");
		for (T v : values) {
			System.out.println("- " + v);
		}
		System.out.println();
	}

	/* Voices:
	
Voices:
- dfki-pavoque-styles
- dfki-spike-hsmm
- bits3-hsmm
- cmu-rms-hsmm
- cmu-bdl-hsmm
- dfki-prudence-hsmm
- dfki-pavoque-neutral
- bits1-hsmm
- voxforge-ru-nsh
- dfki-obadiah-hsmm
- bits3
- dfki-pavoque-neutral-hsmm
- cmu-slt-hsmm
- dfki-poppy-hsmm

Voices[en]:
- cmu-bdl-hsmm
- dfki-prudence-hsmm
- dfki-spike-hsmm
- dfki-obadiah-hsmm
- cmu-slt-hsmm
- cmu-rms-hsmm
- dfki-poppy-hsmm

Voices[en_US]:
- cmu-bdl-hsmm
- cmu-slt-hsmm
- cmu-rms-hsmm

Voices[de]:
- dfki-pavoque-styles
- dfki-pavoque-neutral
- bits1-hsmm
- bits3-hsmm
- dfki-pavoque-neutral-hsmm
- bits3

Voices[ru]:
- voxforge-ru-nsh
	
	
	- cmu-slt
	- dfki-pavoque-styles (de)
	- dfki-spike-hsmm
	- bits3-hsmm (de)
	- cmu-rms-hsmm
	- cmu-bdl-hsmm
	- dfki-prudence-hsmm
	- dfki-pavoque-neutral (de)
	- bits1-hsmm (de)
	- voxforge-ru-nsh
	- dfki-obadiah-hsmm
	- bits3 (de)
	- dfki-pavoque-neutral-hsmm (de)
	- cmu-slt-hsmm
	- dfki-poppy-hsmm
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

/* Audio:
	WAVE_FILE
	AU_FILE
	AIFF_FILE 
 */
	
}
