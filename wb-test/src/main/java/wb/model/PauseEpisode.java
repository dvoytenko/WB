package wb.model;

public class PauseEpisode extends Episode {
	
	public Long pause;

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public String toText() {
		return "Pause";
	}

}
