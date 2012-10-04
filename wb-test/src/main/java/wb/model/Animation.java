package wb.model;

public interface Animation {
	
	void start(Board board);
	
	void frame(long time);
	
	boolean isDone();
	
	void end();

}
