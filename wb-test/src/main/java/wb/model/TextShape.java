package wb.model;

import java.util.ArrayList;
import java.util.List;

public class TextShape extends Shape {
	
	public String text;

//	// Gentium, Mvboli
//	public String fontName;
	
	public Double fontHeight;
	
	public Font font;
	
	// TODO remove
	public Point startPoint;
	
	@Override
	public void draw(final Pane pane) {
		final double baseHeight = this.font.baseHeight;
		final boolean isResize = Math.abs(this.fontHeight - baseHeight) >= 1e-2;
		// final boolean isUpsideDown = this.font.isUpsideDown();
		final TextShape that = this;
		Transform tr = new Transform();
		double scale = isResize ? this.fontHeight/baseHeight : 1;
		tr.scale(scale, -scale);
		pane.withTr(tr, new Runnable() {
			@Override
			public void run() {
				that._draw(pane);
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
						@Override
						public void run() {
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
		return new AnimationImpl();
	}
	
	private class AnimationImpl extends ListAnimation {
		
		private Transform tr;

		public AnimationImpl() {
			List<Animable> list = new ArrayList<Animable>();
			Point p = new Point(0, 0);
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				final Glyph glyph = font.getGlyph(c);
				if (glyph != null) {
					
					// move over
					list.add(new MoveToSegment(p));
					
					// render
					if (glyph.pathSegment != null) {
						Transform tr = new Transform().translate(p.x, p.y);
						list.add(new GlyphShape(glyph, tr));
					}
					
					// offset
					Double advX = glyph.advX;
					if (advX == null) {
						advX = font.baseAdvX;
					}
					if (advX != null) {
						p = p.move(advX, 0);
					}
				}
			}
			setList(list);
		
			final boolean isResize = Math.abs(fontHeight - font.baseHeight) >= 1e-2;
			
			this.tr = new Transform();
			double scale = isResize ? fontHeight/font.baseHeight : 1;
			this.tr.scale(scale, -scale);
			
			// final boolean isUpsideDown = this.font.isUpsideDown();
		}
		
		@Override
		public void start(final Board board) {
			board.withTr(this.tr, new Runnable() {
				@Override
				public void run() {
					AnimationImpl.super.start(board);
				}
			});
		}
		
		@Override
		public void frame(final long time) {
			getBoard().withTr(this.tr, new Runnable() {
				@Override
				public void run() {
					AnimationImpl.super.frame(time);
				}
			});
		}
		
	}
	
	private class GlyphShape extends Shape {
		
		private Glyph glyph;
		
		private Transform tr;

		public GlyphShape(Glyph glyph, Transform tr) {
			this.glyph = glyph;
			this.tr = tr;
		}

		@Override
		public void draw(Pane pane) {
			// not used for drawing
		}

		@Override
		public Animation createAnimation() {
			return new GlyphAnimation(this);
		}

	}
	
	private class GlyphAnimation implements Animation {

		private GlyphShape glyphShape;

		private Board board;

		private Animation anim;

		private Pane pane;

		public GlyphAnimation(GlyphShape glyphShape) {
			this.glyphShape = glyphShape;
		}
		
		@Override
		public void start(final Board board) {
			this.board = board;
			this.pane = board.getAnimationPane();
			this.anim = this.glyphShape.glyph.pathSegment.createAnimation();
			final GlyphAnimation that = this;
			this.board.withTr(this.glyphShape.tr, new Runnable() {
				@Override
				public void run() {
					that.anim.start(board);
				}
			});
		}
		
		@Override
		public void frame(final long time) {
			final GlyphAnimation that = this;
			this.board.withTr(this.glyphShape.tr, new Runnable() {
				@Override
				public void run() {
					that.pane.moveTo(new Point(0, 0));
					that.anim.frame(time);
					that.pane.stroke();
				}
			});
		}

		@Override
		public void end() {
//			this.board.commit(this.glyphShape, true);
//			this.board.getAnimationPane().canvas().clear();
		}
		
		@Override
		public boolean isDone() {
			return this.anim.isDone();
		}
		
	}

}
