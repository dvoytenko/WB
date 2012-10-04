package wb.model;

public class SubAnimation implements Animation {
	
	private Animable source;
	
	private long startTime;

	private Animation animation;

	public SubAnimation(Animable source, long startTime) {
		this.source = source;
		this.startTime = startTime;
		this.animation = source.createAnimation();
	}
	
	public Animable getSource() {
		return source;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	@Override
	public void start(Board board) {
		this.animation.start(board);
	}

	@Override
	public void frame(long time) {
		this.animation.frame(time - this.startTime);
	}

	@Override
	public boolean isDone() {
		return this.animation.isDone();
	}

	@Override
	public void end() {
		this.animation.end();
	}

}
