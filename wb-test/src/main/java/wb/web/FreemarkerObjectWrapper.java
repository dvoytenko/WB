package wb.web;

import freemarker.template.DefaultObjectWrapper;

public class FreemarkerObjectWrapper extends DefaultObjectWrapper {
	
	public FreemarkerObjectWrapper() {
		setExposeFields(true);
	}

}
