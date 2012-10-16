package wb.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.json.JSONException;
import org.json.JSONObject;

import wb.openclipart.OpenClipArtSource;
import wb.util.IoHelper;

/*
 * SVG editor:
 * http://www.flamingtext.com/imagebot/editor?ref=ocal&svgurl=http://openclipart.org/people/molumen/molumen_car_outline_modified.svg&userid=
 */
public class PrepareShape {
	
	public static void main(String[] args) throws Exception {
		
		prepareAndSave("http://openclipart.org/detail/13886/car-outline-modified-by-molumen");
		prepareAndSave("http://openclipart.org/detail/1200/felix-the-cat-by-liftarn");
		prepareAndSave("http://openclipart.org/detail/850/plane-silhouet-by-molumen");
		prepareAndSave("http://openclipart.org/detail/472/adult-and-child-by-liftarn");
		prepareAndSave("http://openclipart.org/detail/854/tramway-by-molumen");
		prepareAndSave("http://openclipart.org/detail/539/umbrella-outline-by-ryanlerch");
		prepareAndSave("http://openclipart.org/detail/630/thinkingboy-outline-by-ryanlerch");
		prepareAndSave("http://openclipart.org/detail/469/boyface8-outline-by-ryanlerch");
		prepareAndSave("http://openclipart.org/detail/22305/coffee-cup-icon-by-pitr-22305");
		prepareAndSave("http://openclipart.org/detail/80/tree-by-peterm");
		prepareAndSave("http://openclipart.org/detail/14504/dog-head-by-nicubunu-14504");
		prepareAndSave("http://openclipart.org/detail/1900/smiling-pig-by-lalolalo");
		prepareAndSave("http://openclipart.org/detail/5642/slamdunk-outline-by-gioppino");
		prepareAndSave("http://openclipart.org/detail/18947/ice-train-by-mbs");
		prepareAndSave("http://openclipart.org/detail/837/wolf-by-liftarn");
		prepareAndSave("http://openclipart.org/detail/1953/minimalist-monitor-and-computer-by-fortran");
		prepareAndSave("http://openclipart.org/detail/23308/trumpet-by-tom-23308");
		prepareAndSave("http://openclipart.org/detail/6069/eiffle-tower-paris-by-shokunin");
		prepareAndSave("http://openclipart.org/detail/7649/eiffel-tower-by-benbois");
		prepareAndSave("http://openclipart.org/detail/12745/eiffel-tower-by-anonymous-12745");
		prepareAndSave("http://openclipart.org/detail/21829/big-ben-houses-of-parliament--by-tom");
		prepareAndSave("http://openclipart.org/detail/5326/art-deco-empire-state-building-by-boort");
		prepareAndSave("http://openclipart.org/detail/11580/fire-tower-by-johnny_automatic");
		
		// (not good) prepare("http://openclipart.org/detail/1186/leaning-tower-of-pisa-by-johnny_automatic");
		// (not good) prepare("http://openclipart.org/detail/6942/coit-tower-from-below-by-stevelambert-6942");
		// (not good) prepare("http://openclipart.org/detail/9333/clock-tower-by-johnny_automatic");
	}

	public static void prepareAndSave(String shapeUrl) throws Exception {
		System.out.println("url: " + shapeUrl);
		
		final ShapeSource source = getSource(shapeUrl);
		
		final ShapeMeta meta = source.getShapeMetaByUrl(shapeUrl);
		System.out.println("meta: " + meta);
		
		final String shapeId = toShapeId(meta);
		System.out.println("id: " + shapeId);
		
		final File dbDir = new File("src/main/webapp/shapedb");
		
		final File svgFile = new File(dbDir, shapeId + ".svg");
		
		// download SVG
		IoHelper.readFile(new URL(meta.svgUrl), svgFile);

		// construct shape
		Shape sourceShape = new SvgParser().parse(svgFile);
		GroupShape shape;
		if (sourceShape instanceof GroupShape) {
			shape = (GroupShape) sourceShape;
		} else {
			shape = new GroupShape();
			shape.shapes.add(sourceShape);
		}
		
		shape.id = shapeId;
		shape.source = meta.source;
		shape.url = meta.url;
		shape.title = meta.title;
		if (shape.title == null) {
			shape.title = shapeId;
		}
		shape.author = meta.author;
		shape.tags = new ArrayList<String>(meta.tags);
		
		// save original image
		saveSvgImage(svgFile, new File(dbDir, shapeId + "-orig.png"), 
				300, 300);
		if (meta.thumbUrl != null) {
			IoHelper.readFile(new URL(meta.thumbUrl), 
					new File(dbDir, shapeId + "-thumb.png"));
		}
		
		// measure shape
		measureShape(shape);
		
		// save converted image
		saveShapeImage(shape, new File(dbDir, shapeId + ".png"),
				200, 200);
		
		// save shape
		saveShape(shape, new File(dbDir, shapeId + ".json"));
		
		// save meta
		saveMeta(shape, new File(dbDir, shapeId + "-meta.json"));
	}

	public static ShapeSource getSource(String shapeUrl) {
		if (shapeUrl.contains("openclipart.org/detail/")) {
			return new OpenClipArtSource();
		}
		return null;
	}

	private static String toShapeId(ShapeMeta meta) {
		String shapeId = "";
		
		shapeId += safe(meta.source) + "-";
		
		String subid = meta.id;
		if (subid == null) {
			subid = meta.url;
		}
		if (subid.endsWith("/")) {
			subid = subid.substring(0, subid.length() - 1);
		}
		if (subid.contains("/")) {
			subid = subid.substring(subid.lastIndexOf('/') + 1);
		}
		shapeId += safe(subid);
		
		return shapeId;
	}

	private static String safe(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c)
					|| c == '_'
					|| c == '-') {
				sb.append(c);
			} else if (c == '.') {
				sb.append('_');
			}
		}
		return sb.toString();
	}

	public static void measureShape(GroupShape shape) {
		
		if (shape.localBounds != null) {
			return;
		}
		
		MeasuringCanvas canvas = new MeasuringCanvas();
		
		Pane pane = new Pane(canvas, new Transform());
		
		shape.draw(pane);
		
		Bounds bounds = canvas.getBounds();
		shape.localBounds = bounds;
		System.out.println("Bounds: " + bounds);
	}

	public static void saveShapeImage(GroupShape shape, File targetFile, 
			int width, int height) throws IOException {
		
		BufferedImage image = new BufferedImage(width, height, 
				BufferedImage.TYPE_INT_ARGB);
		
		RenderingCanvas canvas = new RenderingCanvas(image.createGraphics());
		
		Transform tr = new Transform();
		if (shape.localBounds != null) {
			
			// scale
			double dWidth = width;
			double dHeight = height;
			double lWidth = Math.abs(shape.localBounds.bottomright.x - shape.localBounds.topleft.x);
			double lHeight = Math.abs(shape.localBounds.bottomright.y - shape.localBounds.topleft.y);
			double scaleX = 1.0;
			double scaleY = 1.0;
			if (Math.abs(dWidth - lWidth) > 1e-2) {
				scaleX = dWidth / lWidth;
			}
			if (Math.abs(dHeight - lHeight) > 1e-2) {
				scaleY = dHeight / lHeight;
			}
			double scale = Math.min(scaleX, scaleY);
			tr.scale(scale, scale);

			// compensate
			tr.translate(-shape.localBounds.topleft.x, 
					-shape.localBounds.topleft.y);
			
			// center
			double dx = (dWidth - lWidth * scale) / 2;
			double dy = (dHeight - lHeight * scale) / 2;
			System.out.println("dx/dy: " + dx + "/" + dy);
			tr.translate(dx/scale, dy/scale);

			System.out.println("tr: " + tr);
		}
		
		Pane pane = new Pane(canvas, tr);
		
		shape.draw(pane);
		
	    ImageIO.write(image, "png", targetFile);
	}

	private static void saveSvgImage(File svgFile, File targetFile, double width, double height) 
			throws IOException, TranscoderException {
		
	    final BufferedImage image;
	    
	    Reader in = new InputStreamReader(new FileInputStream(svgFile));
	    try {
			BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();
		    imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
		    imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
		    
		    TranscoderInput input = new TranscoderInput(in);
		    imageTranscoder.transcode(input, null);
		 
		    image = imageTranscoder.getBufferedImage();
	    } finally {
	    	in.close();
	    }
	    
	    ImageIO.write(image, "png", targetFile);
	}

	private static void saveShape(GroupShape shape, File targetFile) 
			throws IOException, JSONException {
		JSONObject js = (JSONObject) new Serializer().toJson(shape);
		Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
		writer.write(js.toString(2));
		writer.close();
	}

	private static void saveMeta(GroupShape shape, File targetFile) 
			throws JSONException, IOException {
		ShapeMeta meta = new ShapeMeta();
		meta.id = shape.id;
		meta.source = shape.source;
		meta.url = shape.url;
		meta.title = shape.title;
		if (meta.title == null) {
			meta.title = shape.id;
		}
		meta.author = shape.author;
		if (shape.tags != null) {
			meta.tags.addAll(shape.tags);
		}
		
		JSONObject js = (JSONObject) new Serializer().toJson(meta);
		Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8");
		writer.write(js.toString(2));
		writer.close();
	}

	private static class BufferedImageTranscoder extends ImageTranscoder {
		
		private BufferedImage img = null;
		
		@Override
		public BufferedImage createImage(int w, int h) {
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			return bi;
		}

		@Override
		public void writeImage(BufferedImage img, TranscoderOutput output) {
			this.img = img;
		}

		public BufferedImage getBufferedImage() {
			return img;
		}
		
	}

	public static void saveShape(ShapeMeta meta) 
			throws IOException, TranscoderException, JSONException {
		
		URL url = new URL(meta.url);
		System.out.println("url: " + url);
		
		if (meta.source == null) {
			meta.source = url.getHost();
		}

		final String shapeId = toShapeId(meta);
		System.out.println("id: " + shapeId);

		final File dbDir = new File("src/main/webapp/shapedb");

		final File svgFile = new File(dbDir, shapeId + ".svg");
		if (meta.svgUrl != null) {
			// download SVG
			IoHelper.readFile(new URL(meta.svgUrl), svgFile);
		}
		
		// construct shape
		Shape sourceShape = meta.shape;
		GroupShape shape;
		if (sourceShape instanceof GroupShape) {
			shape = (GroupShape) sourceShape;
		} else {
			shape = new GroupShape();
			shape.shapes.add(sourceShape);
		}

		shape.id = shapeId;
		shape.source = meta.source;
		shape.url = meta.url;
		shape.title = meta.title;
		if (shape.title == null) {
			shape.title = shapeId;
		}
		shape.author = meta.author;
		shape.tags = new ArrayList<String>(meta.tags);

		// save original image
		saveSvgImage(svgFile, new File(dbDir, shapeId + "-orig.png"), 
				300, 300);

		// measure shape
		measureShape(shape);
		
		// save converted image
		saveShapeImage(shape, new File(dbDir, shapeId + ".png"),
				200, 200);
		
		// save shape
		saveShape(shape, new File(dbDir, shapeId + ".json"));
		
		// save meta
		saveMeta(shape, new File(dbDir, shapeId + "-meta.json"));
	}
	
}