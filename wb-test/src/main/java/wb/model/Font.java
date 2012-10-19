package wb.model;

import java.util.HashMap;
import java.util.Map;

public class Font {
	
	public Map<String, Glyph> glyphMap = new HashMap<String, Glyph>();
	
	public Glyph missingGlyph;
	
	public Double baseHeight;

	public Double baseAdvX;
	
	public Glyph getGlyph(char c) {
		Glyph glyph = this.glyphMap.get(String.valueOf(c));
		if (glyph != null) {
			return glyph;
		}
		return this.missingGlyph;
	}

}
