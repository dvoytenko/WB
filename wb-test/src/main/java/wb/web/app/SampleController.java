package wb.web.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping( { "/sample" })
public class SampleController {

	@RequestMapping(method = RequestMethod.GET, value={"", "/"})
	public ModelAndView get() {
		return new ModelAndView("ok");
	}
	
}
