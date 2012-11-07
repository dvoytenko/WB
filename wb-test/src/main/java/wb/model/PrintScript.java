package wb.model;

import org.json.JSONObject;

import wb.util.IoHelper;

public class PrintScript {

	public static void main(String[] args) throws Exception {

		final String scr = "script2";
		
		System.out.println("Script: " + scr);
		
		JSONObject js = new JSONObject(IoHelper.readText(AAA.class, 
				scr + ".json", "UTF-8"));
		Script script = (Script) new Parser().fromJson(js, Script.class);
		
		for (Episode episode : script.episodes) {
			System.out.println(episode.toText());
		}
		
	}
	
}
