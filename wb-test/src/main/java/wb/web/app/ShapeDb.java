package wb.web.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import wb.model.Parser;
import wb.model.PrepareShape;
import wb.model.ShapeMeta;
import wb.util.IoHelper;

public class ShapeDb implements ApplicationContextAware {

	private ServletContext context;

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		context = ((WebApplicationContext) ctx).getServletContext();
	}

	@SuppressWarnings("unchecked")
	public List<ShapeMeta> getTopShapeMeta() {
		
		List<ShapeMeta> list = new ArrayList<ShapeMeta>();
		
		Set<String> rcList = context.getResourcePaths("/shapedb");
		for (String rc : rcList) {
			if (!rc.endsWith("-meta.json")) {
				continue;
			}
			
			// load shape
			ShapeMeta meta;
			try {
				meta = getMeta(rc);
			} catch (Exception e) {
				meta = null;
				e.printStackTrace();
			}
			if (meta == null) {
				continue;
			}
			
			list.add(meta);
		}
		
		return list;
	}

	private ShapeMeta getMeta(String path) throws JSONException, IOException {
		InputStream stream = context.getResourceAsStream(path);
		try {
			JSONObject js = new JSONObject(IoHelper.readText(stream, "UTF-8"));
			ShapeMeta meta = (ShapeMeta) new Parser().fromJson(js);
			return meta;
		} finally {
			stream.close();
		}
	}

	public void saveShape(ShapeMeta meta) {
		try {
			PrepareShape.saveShape(meta);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
