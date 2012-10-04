package wb.model;

public class AAA {
	
	public void playInBrowser(final Board board, final Script script) {
		board.onReady(script, new Runnable() {
			@Override
			public void run() {
				final Animation animation = board.createAnimation(script);
				final long startTime = System.currentTimeMillis();
				animation.start(board);
				runInFrames(new FrameRunnable() {
					@Override
					public boolean run() {
						if (animation.isDone()) {
							return false;
						}
						long time = System.currentTimeMillis() - startTime;
						animation.frame(time);
						return !animation.isDone();
					}
				});
				animation.end();
			}
		});
	}

	private void runInFrames(final FrameRunnable runnable) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean toContinue = true;
				while (toContinue) {
					try {
						Thread.sleep(100L);
					} catch (InterruptedException e) {
					}
					toContinue = runnable.run();
				}
			}
		}).start();
	}
	
}
