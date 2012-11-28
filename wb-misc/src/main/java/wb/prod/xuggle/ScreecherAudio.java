package wb.prod.xuggle;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

public class ScreecherAudio {
	
	public static void main(String[] args) throws Exception {
		
		final File root = new File("C:\\Work\\WB\\work\\recordings\\t2");
		final File file = new File(root, "wb-sounds-22050.wav");
		
		final File destinationFile = new File(root, "x5.wav");
		
		IMediaWriter writer = ToolFactory.makeWriter(destinationFile.toString());
		writer.addAudioStream(0, 0, 1, 16000);
		
		ScreechStream screechStream = new ScreechStream(file, 
				writer, 0, 22050, 700.0);
		
		BoardState state = new BoardState();
		state.height = 0.0;
		state.velocity = 700.0;
		
		screechStream.update(state);
		
		long frameTime = 0;
		final long frameRate = DEFAULT_TIME_UNIT.convert(1000L / 30L, 
				TimeUnit.MILLISECONDS);
		for (int i = 0; i < 200; i++) {
			frameTime += frameRate;
			screechStream.advanceTo(frameTime);
		}
		
		writer.close();
	}

}
