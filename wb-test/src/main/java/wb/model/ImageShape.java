package wb.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

public class ImageShape extends Shape implements SizeAwareShape {
	
	public Double width;
	
	public Double height;

	public String data;

	@Override
	public Double getWidth() {
		return this.width;
	}

	@Override
	public Double getHeight() {
		return this.height;
	}

	@Override
	public void draw(Pane pane) {
		byte[] bytes = Base64.decodeBase64(this.data);
		BufferedImage img;
		try {
			img = ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pane.drawImage(img, 0, 0, width, height);
	}

	@Override
	public Animation createAnimation() {
		return null;
	}

}
