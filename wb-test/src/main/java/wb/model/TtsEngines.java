package wb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wb.cere.CereEngine;
import wb.ivona.IvonaEngine;
import wb.marytts.MaryEngine;

public class TtsEngines {
	
	private Map<String, TtsEngine> engines = new HashMap<String, TtsEngine>();
	
	public TtsEngines() {
		add(new MaryEngine());
		add(new CereEngine());
		add(new IvonaEngine());
	}
	
	private void add(TtsEngine engine) {
		this.engines.put(engine.getId(), engine);
	}

	public List<TtsEngine> getEngines() {
		return new ArrayList<TtsEngine>(this.engines.values());
	}

	public TtsEngine getEngine(String id) {
		return this.engines.get(id);
	}
	
}
