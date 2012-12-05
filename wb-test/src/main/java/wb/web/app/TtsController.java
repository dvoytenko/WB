package wb.web.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import wb.model.TtsEngine;
import wb.model.TtsEngines;
import wb.model.Voice;
import wb.util.IoHelper;
import wb.web.ServerContext;

@Controller
@RequestMapping( { "/tts" })
public class TtsController {
	
	@Autowired
	private ServerContext serverContext;
	
	private TtsEngines engines = new TtsEngines();
	
	@RequestMapping(method = RequestMethod.GET, value="/getengines.json")
	@ResponseBody
	public List<ValueLabel> getEngines() {
		
		List<TtsEngine> engines = this.engines.getEngines();
		
		List<ValueLabel> list = new ArrayList<ValueLabel>();
		for (TtsEngine engine : engines) {
			list.add(new ValueLabel(engine.getId(), engine.getName()));
		}
		Collections.sort(list, ValueLabel.COMPARATOR_BY_LABEL);
		return list;
	}

	@RequestMapping(method = RequestMethod.GET, value="/getvoices.json")
	@ResponseBody
	public List<ValueLabel> getVoices(@RequestParam("engine") String engineId) {
		TtsEngine engine = this.engines.getEngine(engineId);
		List<Voice> voices = engine.getVoices();
		List<ValueLabel> list = new ArrayList<ValueLabel>();
		for (Voice voice : voices) {
			list.add(new ValueLabel(voice.id, voice.name + 
					(voice.gender != null ? " (" + voice.gender + ")" : "")));
		}
		
		return list;
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value="/gen.json")
	@ResponseBody
	public Result generate(@RequestParam("engine") String engineId,
			@RequestParam("voice") String voiceId,
			@RequestParam("text") String text) {
		if (text == null) {
			return new Result("no text", null);
		}
		
		TtsEngine engine = this.engines.getEngine(engineId);
		if (engine == null) {
			return new Result("unknown engine: " + engineId, null);
		}
		
		if (voiceId == null) {
			voiceId = engine.getDefaultVoiceId();
		}
		
		File file;
		{
			final File tempDir = serverContext.getWorkDir();
			try {
				file = File.createTempFile("wb-audio", ".wav", tempDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		System.out.println("Track file: " + file);
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			engine.generateAudio(text, voiceId, out);
			out.close();
		} catch (IOException e) {
			return new Result("failed to generate audio: " + e, null);
		}
		
		return new Result(null, file.getName());
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value={"/temp/{path:.*}"})
	public void temp(@PathVariable("path") String path,
			HttpServletResponse resp) throws IOException {
		
		if (path.endsWith(".wav")) {
			resp.setContentType("audio/wav");
			File file = new File(serverContext.getWorkDir(), path);
			resp.setContentLength((int) file.length());
			ServletOutputStream out = resp.getOutputStream();
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			IoHelper.copy(in, out);
			in.close();
			out.close();
			return;
		}
		
		throw new RuntimeException("unknown temp file: " + path);
	}
	
	public static class Result {
		
		public String error;
		
		public String track;
		
		public Result(String error, String track) {
			this.track = track;
		}

	}

}
