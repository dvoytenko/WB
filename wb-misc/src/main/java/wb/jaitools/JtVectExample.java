package wb.jaitools;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import com.vividsolutions.jts.geom.Polygon;

import org.jaitools.imageutils.ImageUtils;
import org.jaitools.media.jai.vectorize.VectorizeDescriptor;

public class JtVectExample {

	public static void main(String[] args) {
		new JtVectExample().demo();
	}

	private void demo() {
		final int JAI_TILE_WIDTH = 128;
		final int IMAGE_WIDTH = 240;
		final int SQUARE_WIDTH = 30;

		JAI.setDefaultTileSize(new Dimension(JAI_TILE_WIDTH, JAI_TILE_WIDTH));
		TiledImage src = ImageUtils.createConstantImage(IMAGE_WIDTH, IMAGE_WIDTH, 0);

		Map<String, Object> args = new HashMap<String, Object>();

		/**
		 * Create a chessboard pattern
		 */
		 for (int y = 0; y < IMAGE_WIDTH; y++) {
			 int ysquare = y / SQUARE_WIDTH;

			 for (int x = 0; x < IMAGE_WIDTH; x++) {
				 int xsquare = x / SQUARE_WIDTH;
				 if (xsquare % 2 == ysquare % 2) src.setSample(x, y, 0, 1);
			 }
		 }

		 System.out.println("Source image is a chessboard pattern.");
		 System.out.printf("Image bounds: %d x %d \n", IMAGE_WIDTH, IMAGE_WIDTH);
		 System.out.printf("Chessboard squares: %d x %d \n", SQUARE_WIDTH, SQUARE_WIDTH);
		 System.out.println("Vectorizing:");

		 System.out.println("With default arguments...");
		 Collection<Polygon> polys = doVectorize(src, args);
		 printPolys(polys, 5);

		 System.out.println("With 0 as an outside value...");
		 args.put("outsideValues", Collections.singleton(0));
		 polys = doVectorize(src, args);
		 printPolys(polys, 5);

		 System.out.println("With insideEdges arg set to false...");
		 args.clear();
		 args.put("insideEdges", Boolean.FALSE);
		 polys = doVectorize(src, args);
		 printPolys(polys, 1);

		 System.out.println("With an ROI over central 4x4 chessboard squares...");
		 args.clear();
		 ROI roi = new ROIShape(
				 new Rectangle(2*SQUARE_WIDTH, 2*SQUARE_WIDTH, 4*SQUARE_WIDTH, 4*SQUARE_WIDTH));

		 args.put("roi", roi);
		 polys = doVectorize(src, args);
		 printPolys(polys, 5);
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
	private Collection<Polygon> doVectorize(RenderedImage src, Map<String, Object> args) {
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
    private void printPolys(Collection<Polygon> polys, int nlist) {
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
