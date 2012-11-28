package wb.prod.xuggle;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;


public class ScriptStream {
	
	private File root;
	
	private String[] fragments;
	
	private int pointer = -1;

	public ScriptStream(File root) {
		this.root = root;

		String[] fragments = root.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("0") && name.endsWith(".json");
			}
		});
		Arrays.sort(fragments);
		this.fragments = fragments;
	}
	
	public int getFragmentCount() {
		return this.fragments.length;
	}
	
	public Fragment next() throws IOException, JSONException {
		this.pointer++;
		if (this.pointer >= this.fragments.length) {
			return null;
		}
		return new Fragment(new File(this.root, this.fragments[this.pointer]));
	}
	
}