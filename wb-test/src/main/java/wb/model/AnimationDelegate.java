package wb.model;

public class AnimationDelegate implements Animation {
	
	private Animation animation;
	
	private Board board;

	public AnimationDelegate(Animation animation) {
		this.animation = animation;
	}

	@Override
	public void start(Board board) {
		this.board = board;
		this.animation.start(board);
	}
	
	public Board getBoard() {
		return this.board;
	}

	@Override
	public void frame(long time) {
		this.animation.frame(time);
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
