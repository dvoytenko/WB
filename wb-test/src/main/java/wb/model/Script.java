package wb.model;

import java.util.ArrayList;
import java.util.List;

public class Script {
	
	public List<Episode> episodes = new ArrayList<Episode>();

	public Animation createAnimation() {
		return new ListAnimation(this.episodes);
	}

	public void prepare(PrepareScript preparator) {
		for (Episode episode : this.episodes) {
			episode.prepare(preparator);
		}
	}
	
}
