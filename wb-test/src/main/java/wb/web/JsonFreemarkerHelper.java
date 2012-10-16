package wb.web;

import org.springframework.beans.factory.annotation.Autowired;

public class JsonFreemarkerHelper {
	
	@Autowired
	private JsonMapper jsonMapper;
	
	public void setJsonMapper(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}
	
	public String stringify(Object object) {
		return this.jsonMapper.toJsonString(object);
	}

}
