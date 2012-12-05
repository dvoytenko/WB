package wb.model;

public abstract class Episode implements Animable {
	
	public Integer seq;
	
	public Long pause;

	public void prepare(PrepareScript preparator) {
	}

	public abstract String toText();

}
