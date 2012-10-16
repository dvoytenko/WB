package wb.model;

import java.util.ArrayList;
import java.util.List;

public class ShapeMeta {
	
	public String source;
	
	public String id;
	
	public String url;
	
	public String title;
	
	public String author;
	
	public List<String> tags = new ArrayList<String>();
	
	public String svgUrl;
	
	public String thumbUrl;
	
	public Shape shape;

	@Override
	public String toString() {
		return "ShapeMeta [source=" + source + ", id=" + id + ", url=" + url
				+ ", title=" + title + ", author=" + author + ", tags=" + tags
				+ ", svgUrl=" + svgUrl + ", thumbUrl=" + thumbUrl + "]";
	}

}
