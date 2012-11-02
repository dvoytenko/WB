package wb.model;

import java.io.File;

import org.json.JSONObject;

import wb.util.IoHelper;

public class AAA {
	
	public static void main(String[] args) throws Exception {
		
		final String scr = "script2";
		
		System.out.println("Script: " + scr);
		
		JSONObject js = new JSONObject(IoHelper.readText(AAA.class, 
				scr + ".json", "UTF-8"));
		
		Script script = (Script) new Parser().fromJson(js, Script.class);
		
		File root = new File("src/main/webapp");
		
		File scriptFolder = new File(root, scr);
		if (!scriptFolder.exists()) {
			scriptFolder.mkdir();
		}
		
		File shapesFolder = new File(root, "shapedb");
		File soundsFolder = new File(root, "sounds");
		File fontsFolder = new File(root, "fonts");
		
		PrepareScript prepareScript = new PrepareScript();
		prepareScript.setOutputFolder(scriptFolder);
		prepareScript.setShapesFolder(shapesFolder);
		prepareScript.setSoundsFolder(soundsFolder);
		prepareScript.setFontsFolder(fontsFolder);
		
		script.prepare(prepareScript);
		
		prepareScript.saveObject("script", script);
	}
	
	
	
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
