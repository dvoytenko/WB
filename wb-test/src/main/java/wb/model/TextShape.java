package wb.model;

import java.util.ArrayList;
import java.util.List;

public class TextShape extends Shape {
	
	public String text;

//	// Gentium, Mvboli
//	public String fontName;
	
	public List<Glyph> glyphs;
	
	public Double height;
	
	public Double width;
	
	@Override
	public void prepare(PrepareScript prepareScript) {
		super.prepare(prepareScript);
		
		// TODO customize
		final String fontId = "nova_thin_extended";
		this.prepare(prepareScript.getFont(fontId));
	}
	
	public void prepare(Font font) {
		
		double realHeight = font.baseHeight;
		double realWidth = 0;
		
		// glyphs
		this.glyphs = new ArrayList<Glyph>();
		for (int i = 0; i < this.text.length(); i++) {
			char c = this.text.charAt(i);
			Glyph glyph = font.getGlyph(c);
			if (glyph != null) {
				this.glyphs.add(glyph);
				if (glyph.advX != null) {
					realWidth += glyph.advX;
				} else if (font.baseAdvX != null) {
					realWidth += font.baseAdvX;
				}
			}
		}
		
		// size
		this.height = realHeight;
		this.width = realWidth;
	}
	
	public Transform createTransform() {
		Transform tr = new Transform();
		tr.scale(1, -1);
		tr.translate(0, -this.height);
		return tr;
	}
	
	@Override
	public void draw(final Pane pane) {
//		double baseHeight = this.height;
		// console.log('baseHeight = ' + baseHeight);
		// final boolean isUpsideDown = this.font.isUpsideDown();
		pane.withTr(createTransform(), new Runnable() {
			public void run() {
				_draw(pane);
			}
		});
	}

	private void _draw(final Pane pane) {
		Point p = new Point(0, 0);
		for (int i = 0; i < this.glyphs.size(); i++) {
			final Glyph glyph = this.glyphs.get(i);
			
			// render
			if (glyph.pathSegment != null) {
				Transform tr = new Transform().translate(p.x, p.y);
				pane.withTr(tr, new Runnable() {
					public void run() {
						pane.beginPath();
						pane.moveTo(new Point(0, 0));
						glyph.pathSegment.outline(pane);
						pane.stroke();
					}
				});
			}
			
			// offset
			if (glyph.advX != null) {
				p = p.move(glyph.advX, 0);
			}
		}
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
