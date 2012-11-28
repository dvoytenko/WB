package wb.prod.xuggle;

import org.json.JSONObject;

public class BoardState {
	
	public Double height;
	
	public Double velocity;
	
	public BoardState() {
	}

	public BoardState(JSONObject js) {
		if (js.has("height")) {
			this.height = js.optDouble("height");
		}
		if (js.has("velocity")) {
			this.velocity = js.optDouble("velocity");
		}
	}

}
