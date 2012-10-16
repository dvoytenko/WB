package wb.model;

public interface Canvas {

	void setTransform(double a, double b, double c, double d, double e, double f);

	void save();

	void restore();

	void beginPath();

	void moveTo(double x, double y);

	void lineTo(double x, double y);

	void arc(double cx, double cy, double r, double sAngle, double eAngle,
			boolean counterclockwise);

	void lineWidth(double lineWidth);

	void stroke();

	void drawImage(Object img, double x, double y, double width, double height);

}
