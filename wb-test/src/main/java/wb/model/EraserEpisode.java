package wb.model;

public class EraserEpisode extends Episode {
	
	public boolean eraseAll;

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public String toText() {
		return "Erase";
	}

}
