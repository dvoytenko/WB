package wb.model;

public class Board {
	
	private Pane commitPane;
	
	private Pane animationPane;
	
	private Pane cursorPane;

	private DrawingSoundEngine drawingSoundEngine;
	
	private Cursor cursor;

	private Double defaultBaseVelocity = 100.0;

	private Double baseVelocity = 100.0;
	
	private Double defaultBaseMoveVelocity = 200.0;
	
	private Double defaultBaseChangeAngleVelocity = Geom.PI;
	
	private Point currentPosition;
	
	private Double currentVelocity;

	private Double currentAngle;

	private Double currentPressure;

	private Double currentHeight;

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

	public Point getCurrentPosition(boolean global) {
		if (global || this.currentPosition == null) {
			return this.currentPosition;
		}
		return getAnimationPane().transformGlobalPointToLocal(this.currentPosition);
	}

	public void updateCurrentPosition(Point position, boolean global) {
		if (global || position == null) {
			this.currentPosition = position;
		} else {
			this.currentPosition = getAnimationPane().
					transformLocalPointToGlobal(position);
		}
	}
	
	public Double getCurrentVelocity() {
		return this.currentVelocity;
	}

	public void updateCurrentVelocity(Double velocity) {
		this.currentVelocity = velocity;
	}
	
	public Double getCurrentAngle() {
		return this.currentAngle;
	}

	public void updateCurrentAngle(Double angle) {
		this.currentAngle = angle;
	}
	
	public Double getCurrentPressure() {
		return this.currentPressure;
	}

	public void updateCurrentPressure(Double pressure) {
		this.currentPressure = pressure;
	}
	
	public Double getCurrentHeight() {
		return this.currentHeight;
	}

	public void updateCurrentHeight(Double height) {
		this.currentHeight = height;
	}

	public void commit(Shape shape, boolean render) {
		// TODO
	}

	public Double getBaseVelocity() {
		return this.baseVelocity;
	}
	
	public Double getDefaultBaseVelocity() {
		return this.defaultBaseVelocity;
	}
	
	public Double getBaseRate() {
		return this.baseVelocity/this.defaultBaseVelocity;
	}
	
	public Double getCurrentRate() {
		if (this.currentVelocity == null) {
			return null;
		}
		return this.currentVelocity/this.defaultBaseVelocity;
	}
	
	public Double getBaseChangeAngleVelocity() {
		return defaultBaseChangeAngleVelocity * getBaseRate();
	}

	public Double getBaseMoveVelocity() {
		return defaultBaseMoveVelocity * getBaseRate();
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

	public void withBaseVelocity(final Double baseVelocity, final Runnable runnable) {
		final Double oldBaseVelocity = this.baseVelocity;
		this.baseVelocity = baseVelocity;
		try {
			runnable.run();
		} finally {
			this.baseVelocity = oldBaseVelocity;
		}
	}

	public void withBaseRate(final Double rate, final Runnable runnable) {
		withBaseVelocity(this.baseVelocity * rate, runnable);
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
		return new AnimationImpl(script);
	}
	
	private class AnimationImpl implements Animation {

		private Animation animation;

		public AnimationImpl(Script script) {
			this.animation = script.createAnimation();
		}
		
		@Override
		public void start(Board board) {
			this.animation.start(board);
		}

		@Override
		public void frame(long time) {
			Board.this.beforeFrame(time);
			this.animation.frame(time);
			Board.this.afterFrame(time);
		}

		@Override
		public boolean isDone() {
			return this.isDone();
		}

		@Override
		public void end() {
			this.end();
		}
		
	}

}
