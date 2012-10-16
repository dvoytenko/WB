package wb.model;

public class TextShape extends Shape {
	
	public String text;

//	// Gentium, Mvboli
//	public String fontName;
	
	public Double fontHeight;
	
	public Font font;
	
	// TODO remove
	public Point startPoint;
	
	public Transform createTransform() {
		Transform tr = new Transform();
		tr.translate(this.startPoint.x, this.startPoint.y);
		double baseHeight = this.font.baseHeight;
		boolean isResize = Math.abs(this.fontHeight - baseHeight) >= 1e-2;
		double scale = isResize ? this.fontHeight/baseHeight : 1;
		tr.scale(scale, -scale);
		tr.translate(0, -baseHeight);
		return tr;
	}
	
	@Override
	public void draw(final Pane pane) {
//		double baseHeight = this.font.baseHeight;
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
		for (int i = 0; i < this.text.length(); i++) {
			char c = this.text.charAt(i);
			final Glyph glyph = this.font.getGlyph(c);
			if (glyph != null) {
				
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
				Double advX = glyph.advX;
				if (advX == null) {
					advX = this.font.baseAdvX;
				}
				if (advX != null) {
					p = p.move(advX, 0);
				}
			}
		}
	}

	@Override
	public Animation createAnimation() {
		return null;
	}
	
}
