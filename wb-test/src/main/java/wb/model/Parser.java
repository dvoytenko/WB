package wb.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Parser {

	private SimpleDateFormat df;
	
	public Object fromJson(Object js) throws JSONException {
		return fromJson(js, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object fromJson(Object js, Class<?> type) throws JSONException {
		
		if (js == null) {
			return null;
		}
		
		Object obj;
		
		if (js instanceof JSONArray) {
			JSONArray jsArray = (JSONArray) js;
			List list = new ArrayList();
			for (int i = 0; i < jsArray.length(); i++) {
				list.add(fromJson(jsArray.get(i)));
			}
			obj = list;
		} else if (js instanceof JSONObject) {
			JSONObject jsObj = (JSONObject) js;
			Map opts = new HashMap();
			Iterator keys = jsObj.keys();
			while (keys.hasNext()) {
				String key = keys.next().toString();
				if ("_type".equals(key) || "_desc".equals(key)) {
					continue;
				}
				opts.put(key, fromJson(jsObj.get(key)));
			}
			
			Class theType = type;
			if (theType == null && jsObj.has("_type")) {
				String sType = jsObj.getString("_type");
				if (sType != null) {
					try {
						theType = Class.forName("wb.model." + sType);
					} catch (Exception e) {
						theType = null;
					}
					if (theType == null) {
						throw new RuntimeException("Uknown type '" + sType + "'");
					}
				}
			}
			if (theType != null) {
				
				if (theType == Point.class 
						|| theType == Transform.class
						|| theType == Bounds.class) {
					obj = compatibleType(opts, theType, null);
				} else {
					try {
						obj = theType.newInstance();
					} catch (Exception e) {
						throw new RuntimeException("Failed to create type '" + 
								theType + "': " + e, e);
					}
					
					// set props
					// TODO optimize: cache fields?
					Field[] fields = theType.getFields();
					for (Field field : fields) {
						Object value = opts.get(field.getName());
						if (value == null) {
							continue;
						}
						value = compatibleType(value, field.getType(), field.getGenericType());
						try {
							field.set(obj, value);
						} catch (Exception e) {
							throw new RuntimeException("failed to access field [" + field.getName() 
									+ "] of [" + obj + "]: " + e, e);
						}
					}
				}
			} else {
				obj = opts;
			}
			
		} else {
			obj = compatibleType(js, type, null);
		}
		
		return obj;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object compatibleType(Object value, Type type, Type genericType) {
		
		if (value == null || type == null || 
				value instanceof String || value instanceof Boolean) {
			return value;
		}
		
		if (value instanceof Number) {
			if (type == Integer.class) {
				return ((Number) value).intValue();
			} else {
				return ((Number) value).doubleValue();
			}
		}
		
		if (type == Date.class) {
			if (value instanceof Number) {
				return new Date(((Number) value).longValue());
			}
			return isoDateParse(value.toString());
		}
		
		if (type instanceof Class && ((Class) type).isArray()) {
			List list = (List) value;
			Class elemType = ((Class) type).getComponentType();
			Object arr = Array.newInstance(elemType, list.size());
			for (int i = 0; i < list.size(); i++) {
				Array.set(arr, i, compatibleType(list.get(i), elemType, null));
			}
			return arr;
		}
		
		if (value instanceof List) {
			List list = (List) value;
			List result = new ArrayList(list);
			if (genericType != null &&
					genericType instanceof ParameterizedType) {
				Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
				if (args != null && args.length == 1) {
					Type elementType = args[0];
					result.clear();
					for (Object o : list) {
						result.add(compatibleType(o, elementType, null));
					}
				}
			}
			return result;
		}
		
		if (type == Point.class) {
			if (value instanceof Point) {
				return value;
			}
			return toPoint((Map) value);
		}
		
		if (type == Bounds.class) {
			if (value instanceof Bounds) {
				return value;
			}
			Map map = (Map) value;
			return new Bounds(
					toPoint((Map) map.get("topleft")),
					toPoint((Map) map.get("bottomright")));
		}
		
		if (type == Transform.class) {
			if (value instanceof Transform) {
				return value;
			}
			Map map = (Map) value;
			List m = (List) map.get("m");
			return new Transform(
					((Number) m.get(0)).doubleValue(), 
					((Number) m.get(1)).doubleValue(), 
					((Number) m.get(2)).doubleValue(), 
					((Number) m.get(3)).doubleValue(), 
					((Number) m.get(4)).doubleValue(), 
					((Number) m.get(5)).doubleValue());
		}
		
		return value;
	}

	@SuppressWarnings("rawtypes")
	private Point toPoint(Map map) {
		return new Point(
				((Number) map.get("x")).doubleValue(), 
				((Number) map.get("y")).doubleValue());	
	}

	private Date isoDateParse(String s) {
		if (this.df == null) {
			this.df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		}
		try {
			return this.df.parse(s);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
