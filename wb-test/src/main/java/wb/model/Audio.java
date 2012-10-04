package wb.model;

public interface Audio {

	void stop();

	void setPlaybackRate(double d);

	boolean isPlaying();

	void play(Track track);

}
