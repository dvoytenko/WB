package wb.model;

import java.util.ArrayList;
import java.util.List;

public class ListAnimation implements Animation {
	
	private List<Animable> animableList = new ArrayList<Animable>();
	
	private Board board;
	
	private boolean done;
	
	private List<Animable> completeList = new ArrayList<Animable>();
	
	private List<Animable> pendingList = new ArrayList<Animable>();
	
	private SubAnimation wip = null;
	
	public ListAnimation() {
	}
	
	public ListAnimation(List<? extends Animable> animableList) {
		setList(animableList);
	}
	
	public void setList(List<? extends Animable> animableList) {
		this.animableList.addAll(animableList);
	}
	
	@Override
	public void start(Board board) {
		this.board = board;
		this.completeList.clear();
		this.pendingList.clear();
		this.pendingList.addAll(animableList);
		this.done = this.pendingList.isEmpty();
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
		
		if (wip == null && !this.pendingList.isEmpty()) {
			do {
				if (wip != null) {
					wip.end();
					this.completeList.add(wip.getSource());
					wip = null;
				}
				if (!this.pendingList.isEmpty()) {
					Animable part = this.pendingList.remove(0);
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
		
		this.done = this.pendingList.isEmpty() && wip == null;
	}

}
