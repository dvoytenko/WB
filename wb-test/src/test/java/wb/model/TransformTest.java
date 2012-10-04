package wb.model;

import junit.framework.TestCase;

public class TransformTest extends TestCase {
	
	public void testPoint() throws Exception {
		
		Transform tr = new Transform().translate(100, 200);
		
		// global (0,0) -> local(100,200)
		assertEquals(new Point(100, 200), tr.transformPoint(new Point(0, 0)));
		
		// local(100,200) -> global(0,0)
		Transform inv = new Transform(tr).invert();
		assertEquals(new Point(0, 0), inv.transformPoint(new Point(100, 200)));
	}

}
