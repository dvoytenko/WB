package wb.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Serializer {
	
	private SimpleDateFormat df;

	public void toJson(Object obj, File targetFile) throws IOException, JSONException {
		JSONObject js = (JSONObject) toJson(obj);
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(targetFile), "UTF-8"));
		writer.write(js.toString(2));
		writer.close();
	}

	@SuppressWarnings("rawtypes")
	public Object toJson(Object obj) throws JSONException {
		
		if (obj == null) {
			return null;
		}
		
		if (obj instanceof String || obj instanceof Number ||
				obj instanceof Boolean) {
			return obj;
		}
		
		if (obj instanceof Date) {
			return isoDateFormat((Date) obj);
		}
		
		if (obj.getClass().isArray()) {
			JSONArray array = new JSONArray();
			int length = Array.getLength(obj);
			for (int i = 0; i < length; i++) {
				array.put(toJson(Array.get(obj, i)));
			}
			return array;
		}
		if (obj instanceof Collection) {
			JSONArray array = new JSONArray();
			Collection c = (Collection) obj;
			for (Object o : c) {
				array.put(toJson(o));
			}
			return array;
		}
		
		if (obj instanceof Map) {
			Map map = (Map) obj;
			JSONObject jsMap = new JSONObject();
			for (Object key : map.keySet()) {
				jsMap.put(key.toString(), toJson(map.get(key)));
			}
			return jsMap;
		}
		
		if (obj instanceof Point) {
			return pointToJson((Point) obj);
		}

		if (obj instanceof Bounds) {
			return boundsToJson((Bounds) obj);
		}
		
		if (obj.getClass().getPackage().getName().equals("wb.model")) {
			// TODO optimize: cache fields?
			JSONObject js = new JSONObject();
			js.put("_type", obj.getClass().getSimpleName());
			Field[] fields = obj.getClass().getFields();
			for (Field field : fields) {
				Object value;
				try {
					value = field.get(obj);
				} catch (Exception e) {
					System.out.println("failed to access field [" + field.getName() 
							+ "] of [" + obj + "]");
					value = null;
				}
				if (value != null) {
					js.put(field.getName(), toJson(value));
				}
			}
			return js;
		}
		
		throw new IllegalArgumentException("cannot convert [" + obj + "] to JSON");
	}

	private JSONObject boundsToJson(Bounds obj) throws JSONException {
		JSONObject js = new JSONObject();
		Bounds b = (Bounds) obj;
		js.put("topleft", pointToJson(b.topleft));
		js.put("bottomright", pointToJson(b.bottomright));
		return js;
	}

	private JSONObject pointToJson(Point p) throws JSONException {
		JSONObject js = new JSONObject();
		js.put("x", p.x);
		js.put("y", p.y);
		return js;
	}

	private String isoDateFormat(Date date) {
		if (this.df == null) {
			this.df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		}
		return this.df.format(date);
	}

}
