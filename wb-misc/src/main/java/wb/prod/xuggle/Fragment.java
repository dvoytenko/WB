package wb.prod.xuggle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Fragment {
	
	public final String name;

	public final int index;
	
	public String speech;
	
	public BoardState boardState;

	public Fragment(File file) throws IOException, JSONException {
		
		this.name = file.getName().replace(".json", "");
		this.index = Integer.parseInt(this.name);
		
		JSONObject js;
		Reader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		try {
			js = new JSONObject(new JSONTokener(reader));
		} finally {
			reader.close();
		}
		
		this.speech = js.optString("speech");
		if ("null".equals(this.speech) || "".equals(this.speech)) {
			this.speech = null;
		}
		if (this.speech != null) {
			this.speech = this.speech.substring(this.speech.lastIndexOf('/') + 1);
		}
		
		JSONObject jsState = js.optJSONObject("state");
		if (jsState != null) {
			this.boardState = new BoardState(jsState);
		}
	}
	
}