package wb.model;

public abstract class Episode implements Animable {
	
	public Long pause;

	public void prepare(PrepareScript preparator) {
	}

	public abstract String toText();

}
