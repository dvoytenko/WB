package wb.model;

public class DrawingSoundEngine {
	
	private Audio audio;
	
	private Track track;
	
	private double normalVelocity;
	
	private Double previousVelocity;

	public void update(Board board) {
		
		if (board.getCurrentHeight() > 0.0 || board.getCurrentVelocity() == 0.0) {
			audio.stop();
		} else {
			if (this.previousVelocity != board.getCurrentVelocity()) {
				double rate = board.getCurrentVelocity()/normalVelocity;
				rate = Math.max(Math.min(rate, 1.3), 0.7);
				audio.setPlaybackRate(rate);
			}
			if (!audio.isPlaying()) {
				audio.play(track);
			}
		}
		
		this.previousVelocity = board.getCurrentVelocity();
		
		// pressure
	}

}
