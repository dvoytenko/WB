package wb.model;

import java.util.ArrayList;
import java.util.List;

public class GroupShape extends Shape {
	
	public Transform transform;
	
	public List<Shape> shapes = new ArrayList<Shape>();
	
	@Override
	public Point getStartPoint() {
		if (this.shapes == null || this.shapes.isEmpty()) {
			return null;
		}
		return this.shapes.get(0).getStartPoint();
	}
	
	@Override
	public Animation createAnimation() {
		List<Animable> animations = new ArrayList<Animable>();
		for (Shape shape : this.shapes) {
			Point point = shape.getStartPoint();
			if (point != null) {
				animations.add(new MoveToSegment(point));
			}
			animations.add(shape);
		}
		return new AnimationImpl(animations);
	}
	
	@Override
	public void draw(final Pane pane) {
		pane.withTr(this.transform, new Runnable() {
			@Override
			public void run() {
				for (Shape shape : GroupShape.this.shapes) {
					shape.draw(pane);
				}
			}
		});
	}
	
	private class AnimationImpl extends ListAnimation {

		public AnimationImpl(List<? extends Animable> animableList) {
			super(animableList);
		}
		
		@Override
		public void start(final Board board) {
			board.withTr(GroupShape.this.transform, new Runnable() {
				@Override
				public void run() {
					AnimationImpl.super.start(board);
				}
			});
		}
		
		@Override
		public void frame(final long time) {
			getBoard().withTr(GroupShape.this.transform, new Runnable() {
				@Override
				public void run() {
					AnimationImpl.super.frame(time);
				}
			});
		}

	}

}
