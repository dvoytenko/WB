package wb.model;

import java.util.List;

public class Script {
	
	public List<Episode> episodes;

	public Animation createAnimation() {
		return new ListAnimation(this.episodes);
	}
	
}
