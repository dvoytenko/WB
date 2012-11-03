package wb.model;

public abstract class Shape implements Animable {
	
	public void prepare(PrepareScript prepareScript) {
	}

	public abstract void draw(Pane pane);
	
}
