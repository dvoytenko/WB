package wb.marytts;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.MaryInterface;
import marytts.client.RemoteMaryInterface;

import wb.model.TtsEngine;
import wb.model.Voice;

public class MaryEngine implements TtsEngine {
	
	@Override
	public String getId() {
		return "mary";
	}
	
	@Override
	public String getName() {
		return "Mary";
	}
	
	@Override
	public List<Voice> getVoices() {
		List<Voice> voices = new ArrayList<Voice>();
		voices.add(new Voice("cmu-slt-hsmm", "Anna", "Female Markoff", "female", "en", "US", "en_US"));
		voices.add(new Voice("cmu-bdl-hsmm", "cmu-bdl-hsmm", "cmu-bdl-hsmm", "male", "en", "US", "en_US"));
		voices.add(new Voice("cmu-slt", "cmu-slt", "cmu-slt", "female", "en", "US", "en_US"));
		voices.add(new Voice("cmu-rms-hsmm", "cmu-rms-hsmm", "cmu-rms-hsmm", "male", "en", "US", "en_US"));
		voices.add(new Voice("dfki-prudence-hsmm", "Rachel", "British female prudence", "female", "en", "GB", "prudence"));
		voices.add(new Voice("dfki-spike-hsmm", "dfki-spike-hsmm", "dfki-spike-hsmm", "male", "en", "GB", "spike"));
		voices.add(new Voice("dfki-obadiah-hsmm", "dfki-obadiah-hsmm", "dfki-obadiah-hsmm", "male", "en", "GB", "obadiah"));
		voices.add(new Voice("dfki-poppy-hsmm", "dfki-poppy-hsmm", "dfki-poppy-hsmm", "female", "en", "GB", "poppy"));
		return voices;
	}

	@Override
	public String getDefaultVoiceId() {
		return "cmu-slt-hsmm";
	}

	@Override
	public void generateAudio(String text, String voiceId, OutputStream output)
			throws IOException {
		try {
			MaryInterface marytts = new RemoteMaryInterface();
			marytts.setVoice(voiceId);
			marytts.setOutputType("AUDIO");
			
			AudioInputStream audio = marytts.generateAudio(text);
			
			AudioSystem.write(audio, AudioFileFormat.Type.WAVE, output);
		} catch (Exception e) {
			throw new RuntimeException("failed to generate audio: " + e, e);
		}
	}

}
