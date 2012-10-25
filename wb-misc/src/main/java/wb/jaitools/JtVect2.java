package wb.jaitools;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.jaitools.media.jai.vectorize.VectorizeDescriptor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class JtVect2 {

	public static void main(String[] aargs) throws Exception {

		File root = new File("target");
		final String image = "phone2.jpg";

		final BufferedImage src = ImageIO.read(new File(root, image));
		System.out.println(src.getWidth() + " x " + src.getHeight());

		Map<String, Object> args = new HashMap<String, Object>();
		// http://jaitools.org/docs/jaitools/stable/apidocs/
		// args.put("outsideValues", Collections.singleton(0)); // o0
		// args.put("insideEdges", Boolean.FALSE); // ie0
		// args.put("removeCollinear", Boolean.TRUE); // rc
		// args.put("roi", new ROI); // roi
		args.put("filterThreshold", 5.1);
		args.put("filterMethod", VectorizeDescriptor.FILTER_MERGE_LARGEST);
		 
		// 256 x 362
		
		Collection<Polygon> polys = doVectorize(src, args);
		printPolys(polys, 5);
		
		writePolys(polys, new File(root, image + "-f51ml.svg"), src.getWidth(), src.getHeight());
	}

	private static void writePolys(Collection<Polygon> polys, File file, 
			int width, int height) throws IOException {
		PrintWriter writer = new PrintWriter(file);
		writer.println("<?xml version='1.0'?>");
		writer.println("<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>");
		
		for (Polygon poly : polys) {
			if (isBorder(poly, width, height)) {
				continue;
			}
			// external ring
			writer.println("<polygon style='fill:none;stroke:black;stroke-width:1'");
			writer.println("  points='" + toSvgPoints(poly.getExteriorRing()) + "'");
			writer.println("/>");
			
			// internal ring?
			/*
		      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
		        writer.write(", ");
		        appendLineStringText(polygon.getInteriorRingN(i), level + 1, true, writer);
		      }
			 */
		}
		
		writer.println("</svg>");
		writer.close();
	}

	private static boolean isBorder(Polygon poly, int width, int height) {
		// width height, width 0, 0 0, 0 height, width height
		for (int i = 0; i < poly.getExteriorRing().getNumPoints(); i++) {
			Coordinate c = poly.getExteriorRing().getCoordinateN(i);
			if (c.x == 0.0 && c.y == 0.0) {
				// top-left
			} else if (c.x == width && c.y == 0) {
				// top-right
			} else if (c.x == width && c.y == height) {
				// bottom-right
			} else if (c.x == 0 && c.y == height) {
				// bottom-left
			} else {
				return false;
			}
		}
		return true;
	}

	private static String toSvgPoints(LineString lineString) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < lineString.getNumPoints(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			
			Coordinate coordinate = lineString.getCoordinateN(i);
		    sb.append(writeNumber(coordinate.x) + " " + writeNumber(coordinate.y));
//		    if (outputDimension >= 3 && ! Double.isNaN(coordinate.z)) {
//		      writer.write(" ");
//		      writer.write(writeNumber(coordinate.z));
//		    }
		}
		
		return sb.toString();
	}

	private static String writeNumber(double x) {
		return String.valueOf(x);
	}

	/**
	 * Helper function to run the Vectorize operation with given parameters and
	 * retrieve the vectors.
	 * 
	 * @param src the source image
	 * @param args a {@code Map} of parameter names and values
	 * 
	 * @return the generated vectors as JTS Polygons
	 */
	@SuppressWarnings("unchecked")
	private static Collection<Polygon> doVectorize(RenderedImage src, Map<String, Object> args) {
		ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
		pb.setSource("source0", src);

		// Set any parameters that were passed in
		for (Entry<String, Object> e : args.entrySet()) {
			pb.setParameter(e.getKey(), e.getValue());
		}

		// Get the desintation image: this is the unmodified source image data
		// plus a property for the generated vectors
		RenderedOp dest = JAI.create("Vectorize", pb);

		// Get the vectors
		Object property = dest.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);
		return (Collection<Polygon>) property;
	}

	/**
	 * Print a summary of the polygons
	 * 
	 * @param polys the polygons
	 * @param nlist the number to print as Well Known Text (WKT) string
	 */
	private static void printPolys(Collection<Polygon> polys, int nlist) {
		int n = polys.size();
		System.out.printf("   Got %d polygon%s \n", n, (n == 1 ? "" : "s"));

		nlist = Math.min(nlist, polys.size());
		if (nlist > 0) {
			Iterator<Polygon> iter = polys.iterator();
			int k = 0;
			while (k < nlist) {
				Polygon p = iter.next();
				p.normalize();

				// print the polygon as a WKT string
				System.out.println("   " + p.toText());

				// get the value of the source image region bounded by this polygon
				// (this is stored as "user data" in the Polygon object)
				System.out.println("   Image value: " + p.getUserData());
				k++ ;
			}

			if (k < polys.size()) System.out.println("   ...");
			System.out.println();
		}
	}

}
