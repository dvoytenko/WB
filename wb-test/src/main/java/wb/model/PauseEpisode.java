package wb.model;

public class PauseEpisode extends Episode {

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public String toText() {
		return "Pause";
	}

}
