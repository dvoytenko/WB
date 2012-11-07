package wb.model;

public class PanEpisode extends Episode {
	
	public Point point;

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public String toText() {
		return "Pan board";
	}

}
