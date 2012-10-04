package wb.model;

public abstract class Shape implements Animable {
	
	public abstract Point getStartPoint();

	public abstract void draw(Pane pane);
	
}
