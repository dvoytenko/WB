package wb.model;

import java.util.ArrayList;
import java.util.List;

public abstract class IterAnimation implements Animation {

	private Board board;
	
	private List<Animable> completeList = new ArrayList<Animable>();

	private boolean done;

	private SubAnimation wip = null;
	
	protected abstract boolean hasNext();

	protected abstract Animable next();
	
	@Override
	public void start(Board board) {
		this.board = board;
		this.completeList.clear();
		this.done = !hasNext();
	}
	
	public Board getBoard() {
		return board;
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
		
		if (wip == null && this.hasNext()) {
			do {
				if (wip != null) {
					wip.end();
					this.completeList.add(wip.getSource());
					wip = null;
				}
				if (this.hasNext()) {
					Animable part = next();
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
	    	this.completeList.add(wip.getSource());
	    	wip = null;
		}
		
		this.done = !hasNext() && wip == null;
	}

}
