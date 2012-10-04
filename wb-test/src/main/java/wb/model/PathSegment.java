package wb.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathSegment extends Segment {

	public Point startPoint;
	
	public List<Segment> segments = new ArrayList<Segment>();
	
	public PathSegment() {
	}

	public PathSegment(List<Segment> segments) {
		this.segments.addAll(segments);
	}

	public Point getStartPoint() {
		if (this.startPoint != null) {
			return this.startPoint;
		}
		return null;
	}
	
	@Override
	public Double getStartAngle(Pane pane) {
		if (segments == null || segments.isEmpty()) {
			return null;
		}
		return segments.get(0).getStartAngle(pane);
	}
	
	@Override
	public void outline(Pane pane) {
		if (this.startPoint != null) {
			pane.moveTo(this.startPoint);
		}
		for (Segment segment : this.segments) {
			segment.outline(pane);
		}
	}
	
	@Override
	public Segment[] expand() {
		ArrayList<Segment> list = new ArrayList<Segment>();
		for (Segment s : this.segments) {
			list.addAll(Arrays.asList(s.expand()));
		}
		return list.toArray(new Segment[list.size()]);
	}
	
	@Override
	public Animation createAnimation() {
		return new AnimationImpl();
	}
	
	private class AnimationImpl implements Animation {

		private Board board;
		
		private boolean done;
		
		private List<Segment> completeList = new ArrayList<Segment>();
		
		private List<Segment> pendingList = new ArrayList<Segment>();
		
		private SubAnimation wip = null;

		private Pane pane;
		
		@Override
		public void start(Board board) {
			this.board = board;
			this.pane = this.board.getAnimationPane();
			
			this.completeList.clear();
			this.pendingList.clear();
			
			Segment[] segments = expand();
			for (Segment segment : segments) {
				if (!(segment instanceof MoveToSegment)) {
					this.pendingList.add(new ChangeAngleSegment(segment));
				}
				this.pendingList.add(segment);
			}
			this.done = this.pendingList.isEmpty();
		}

		@Override
		public boolean isDone() {
			return this.done;
		}

		@Override
		public void end() {
		}
		
		@Override
		public void frame(long time) {

			this.pane.beginPath();
			
			for (Segment complete : this.completeList) {
				complete.outline(this.pane);
			}
			
			if (wip == null && !this.pendingList.isEmpty()) {
				do {
					if (wip != null) {
						wip.end();
						this.completeList.add((Segment) wip.getSource());
						wip = null;
					}
					if (!this.pendingList.isEmpty()) {
						Segment part = this.pendingList.remove(0);
						wip = new SubAnimation(part, time);
						wip.start(this.board);
					}
				} while (wip != null && wip.isDone());
			}

			if (wip != null && !wip.isDone()) {
		    	wip.frame(time);
			}

			if (wip != null && wip.isDone()) {
				wip.end();
		    	this.completeList.add((Segment) wip.getSource());
		    	wip = null;
			}
			
			this.done = this.pendingList.isEmpty() && wip == null;
		}
		
	}

}
