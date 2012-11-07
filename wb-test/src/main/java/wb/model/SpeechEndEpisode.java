package wb.model;

public class SpeechEndEpisode extends Episode {

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public String toText() {
		return "Wait for speech to finish";
	}

}
