package wb.model;

public class PanZoomEpisode extends Episode {
	
	public Double zoomFactor;
	
	public Point panTo;

	public Point origin;

	@Override
	public Animation createAnimation() {
		return null;
	}

	@Override
	public String toText() {
		return "Pan/Zoom board";
	}

}
