package wb.model;

public abstract class Segment implements Animable {

	public abstract Double getStartAngle(Pane pane);

	public abstract void outline(Pane pane);

	public Segment[] expand() {
		return new Segment[] {this};
	}

}
