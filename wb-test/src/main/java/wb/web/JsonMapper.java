package wb.web;

import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonMapper {
	
	private ObjectMapper objectMapper;
	
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public String toJsonString(Object object) {
		try {
			StringWriter w = new StringWriter();
			this.objectMapper.writeValue(w, object);
			return w.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
