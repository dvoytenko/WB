package gtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class ABC {
	
	public static void main(String[] args) {
		new ABC().priorityQueue();
	}
	
	public void x() {
		
	}
	
	public void shuffle() {
		List<String> list = new ArrayList<String>();
		Collections.shuffle(list);
	}
	
	public void hashMap() {
		HashMap<String, String> map = new HashMap<String, String>();
	}
	
	public void treeSet() {
		TreeSet<String> treeSet = new TreeSet<String>();
	}

	public void priorityQueue() {
		PriorityQueue<String> queue = new PriorityQueue<String>();
		queue.offer("B");
		queue.offer("C");
		queue.offer("D");
		queue.offer("E");
		queue.offer("F");
		queue.offer("G");
		System.out.println(queue);
		queue.offer("A");
		System.out.println(queue);
		String s;
		while ((s = queue.poll()) != null) {
			System.out.println("- " + s);
		}
	}

}
