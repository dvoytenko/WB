package wb.model;

public class Bounds {
	
	// can't make topleft and bottomright final b/c of parsers
	public Point topleft;
	
	public Point bottomright;
	
	public Bounds(Point topleft, Point bottomright) {
		this.topleft = topleft;
		this.bottomright = bottomright;
	}

	@Override
	public String toString() {
		return "Bounds [topleft=" + topleft + ", bottomright=" + bottomright
				+ "]";
	}

}
