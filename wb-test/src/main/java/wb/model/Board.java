package wb.model;

public class Board {
	
	private Pane commitPane;
	
	private Pane animationPane;
	
	private Pane cursorPane;

	private DrawingSoundEngine drawingSoundEngine;
	
	private Cursor cursor;

	public void onReady(Script script, Runnable runnable) {
		// TODO check script's episodes are ready
		runnable.run();
	}

	public Pane getCommitPane() {
		return this.commitPane;
	}

	public Pane getAnimationPane() {
		return this.animationPane;
	}
	
	public Pane getCursorPane() {
		return cursorPane;
	}

	public void commitShape(Shape shape, boolean render) {
		// TODO
	}

	public void withTr(final Transform transform, final Runnable runnable) {
		if (transform != null) {
			final Pane pane1 = this.commitPane;
			final Pane pane2 = this.animationPane;
			pane1.withTr(transform, new Runnable() {
				@Override
				public void run() {
					pane2.withTr(transform, runnable);
				}
			});
		} else {
			runnable.run();
		}
	}

	public void beforeFrame(long time) {
	}

	public void afterFrame(long time) {
		this.update(time);
		this.drawingSoundEngine.update(this);
		this.cursor.update(this);
	}
	
	public void update(long time) {
	}

	public Animation createAnimation(Script script) {
		return null;
	}
	
}
