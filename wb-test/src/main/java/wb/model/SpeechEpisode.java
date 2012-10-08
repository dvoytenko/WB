package wb.model;

public class SpeechEpisode extends Episode {
	
	public String track;
	
	public String text;
	
	public String voice;
	
	@Override
	public void prepare(PrepareScript preparator) {
		super.prepare(preparator);
		if (this.track == null && this.text != null) {
			String name = preparator.createAudio(this.text, this.voice);
			this.track = name;
		}
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
