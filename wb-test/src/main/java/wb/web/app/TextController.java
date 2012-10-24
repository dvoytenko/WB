package wb.web.app;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import wb.model.Font;
import wb.model.Parser;
import wb.model.TextShape;

@Controller
@RequestMapping( { "/text" })
public class TextController implements ApplicationContextAware {
	
	private Font font;
	
	private ServletContext servletContext;
	
	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.servletContext = ((WebApplicationContext) context).getServletContext();
	}
	
	private Font getFont() {
		if (this.font != null) {
			return this.font;
		}
		
		// TODO customize
		final String fontId = "nova_thin_extended";
		
		Font font;
		
		try {
			JSONObject js;
			Reader reader = new InputStreamReader(this.servletContext.
					getResourceAsStream("/fonts/" + fontId + ".json"), "UTF-8");
			try {
				js = new JSONObject(new JSONTokener(reader));
			} finally {
				reader.close();
			}
			
			Parser parser = new Parser();
			font = (Font) parser.fromJson(js, Font.class);
		} catch (Exception e) {
			throw new RuntimeException("can't get font [" + fontId + "]: " + e, e);
		}
		
		this.font = font;
		return font;
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value="/getshape.json")
	@ResponseBody
	public TextShape getShape(@RequestParam("text") String text) {
		
		TextShape shape = new TextShape();
		shape.text = text;
		
		Font font = getFont();
		
		shape.prepare(font);
		
		return shape;
	}

}
