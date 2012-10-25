package wb.jaitools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import com.vividsolutions.jts.geom.LineString;

import org.jaitools.media.jai.contour.ContourDescriptor;
import org.jaitools.swing.ImageFrame;
import org.jaitools.swing.JTSFrame;

public class JtContour2 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		JAI.setDefaultTileSize(new Dimension(512, 512));

		File root = new File("target");
		final String imagename = "openclipart_org--by--3572.png";

		final BufferedImage image = ImageIO.read(new File(root, imagename));
		System.out.println(image.getWidth() + " x " + image.getHeight());

		List<Double> contourIntervals = new ArrayList<Double>();

		for (double level = 0.2; level < 1.41; level += 0.2) {
			contourIntervals.add(level);
		}

		ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
		pb.setSource("source0", image);
		pb.setParameter("levels", contourIntervals);

		RenderedOp dest = JAI.create("Contour", pb);
		Collection<LineString> property = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
		Collection<LineString> contours = property;

		JTSFrame jtsFrame = new JTSFrame("Contours from source image");
		for (LineString contour : contours) {
			jtsFrame.addGeometry(contour, Color.BLUE);
		}

		ImageFrame imgFrame = new ImageFrame(image, "Source image");
		imgFrame.setLocation(100, 100);
		imgFrame.setVisible(true);

		Dimension size = imgFrame.getSize();
		jtsFrame.setSize(size);
		jtsFrame.setLocation(100 + size.width + 5, 100);
		jtsFrame.setVisible(true);		
	}

}
