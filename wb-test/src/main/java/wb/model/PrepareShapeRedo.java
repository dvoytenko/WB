package wb.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class PrepareShapeRedo {
	
	public static void main(String[] args) throws Exception {
//		redoAll();
		redo("www_clker_com-whiteboard_svg");
	}

	public static void redoAll() throws Exception {
		
		final File dbDir = new File("src/main/webapp/shapedb");
		String[] files = dbDir.list();
		Arrays.sort(files);
		for (String file : files) {
			if (file.endsWith(".svg")) {
				redo(file.replace(".svg", ""));
			}
		}
	}

	public static void redo(String shapeId) throws Exception {
		System.out.println("id: " + shapeId);

		final File dbDir = new File("src/main/webapp/shapedb");
		
		final File svgFile = new File(dbDir, shapeId + ".svg");
		
		// load/convert svg
		Shape sourceShape = new SvgParser().parse(svgFile);

		// prepare shape
		GroupShape shape = PrepareShape.prepareShape(sourceShape);
		
		// load meta
		ShapeMeta meta = new Parser().fromJsonFile(new File(dbDir, shapeId + "-meta.json"),
				ShapeMeta.class);
		shape.id = meta.id;
		shape.source = meta.source;
		shape.url = meta.url;
		shape.title = meta.title;
		shape.author = meta.author;
		if (meta.tags != null && !meta.tags.isEmpty()) {
			shape.tags = new ArrayList<String>(meta.tags);
		}

		// save converted image
		PrepareShape.saveShapeImage(shape, new File(dbDir, shapeId + ".png"),
				200, 200);
		
		// save shape
		PrepareShape.saveShape(shape, new File(dbDir, shapeId + ".json"));
		
		// save meta
		PrepareShape.saveMeta(shape, new File(dbDir, shapeId + "-meta.json"));
	}

}
