package wb.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface TtsEngine {
	
	String getId();
	
	String getName();

	List<Voice> getVoices();

	String getDefaultVoiceId();
	
	void generateAudio(String text, String voiceId, OutputStream output)
		throws IOException;
	
}
