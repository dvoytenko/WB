package wb.web;

import java.io.IOException;
import java.util.Iterator;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.NumericNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wb.model.Parser;
import wb.model.Segment;
import wb.model.Serializer;
import wb.model.Shape;
import wb.model.ShapeMeta;

public class CustomJacksonMapper extends ObjectMapper {
	
	public CustomJacksonMapper() {
		super();
		configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		SimpleModule module = new SimpleModule("WB", new Version(1, 0, 0, null))
			.addSerializer(ShapeMeta.class, new WbSerializer<ShapeMeta>())
			.addDeserializer(ShapeMeta.class, new ShapeMetaDeserializer())
			.addSerializer(Shape.class, new WbSerializer<Shape>())
			.addSerializer(Segment.class, new WbSerializer<Segment>())
			;
		registerModule(module);
	}
	
	private static Object conv(JsonNode v) throws JSONException {
		if (v == null || v.isNull() || v.isMissingNode()) {
			return null;
		}
		
		if (v.isObject()) {
			ObjectNode n = (ObjectNode) v;
			JSONObject js = new JSONObject();
			for (Iterator<String> i = n.getFieldNames(); i.hasNext();) {
				String key = i.next();
				js.put(key, conv(n.get(key)));
			}
			return js;
		}
		if (v.isArray()) {
			ArrayNode n = (ArrayNode) v;
			JSONArray array = new JSONArray();
			for (JsonNode elem : n) {
				array.put(conv(elem));
			}
			return array;
		}
		if (v.isBoolean()) {
			return ((BooleanNode) v).asBoolean();
		}
		if (v.isInt()) {
			return ((NumericNode) v).asInt();
		}
		if (v.isLong()) {
			return ((NumericNode) v).asLong();
		}
		if (v.isNumber()) {
			return ((NumericNode) v).asDouble();
		}
		if (v.isTextual()) {
			return ((TextNode) v).asText();
		}
		throw new JSONException("cannot convert [" + v + "]"); 
	}
	
	private static class ShapeMetaDeserializer extends JsonDeserializer<ShapeMeta> {

		@Override
		public ShapeMeta deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			JsonNode v = jp.readValueAsTree();
			try {
				JSONObject js = (JSONObject) conv(v);
				return (ShapeMeta) new Parser().fromJson(js, ShapeMeta.class);
			} catch (JSONException e) {
				throw new JsonMappingException("failed to parse ShapeMeta: " + e, e);
			}
		}

	}
	
	private static class WbSerializer<T> extends JsonSerializer<T> {

		@Override
		public void serialize(T value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			Object js;
			try {
				js = new Serializer().toJson(value);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			jgen.writeRawValue(js.toString());
		}
		
	}

}
