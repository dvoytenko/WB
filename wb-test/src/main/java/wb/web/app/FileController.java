package wb.web.app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping( { "/file" })
public class FileController implements ApplicationContextAware {
	
	private File uploadFir;
	
	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		String s = System.getProperty("upload.dir");
		if (s != null) {
			this.uploadFir = new File(s);
		}
	}
	
	@RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST}, value="/upload")
	public String upload(@RequestParam("file") MultipartFile file) throws IllegalStateException, IOException {
		
		System.out.println(file.getOriginalFilename());
		System.out.println(file.getContentType());
		System.out.println(file.getSize());
		System.out.println(file);
		
		File destFile = new File(this.uploadFir, file.getOriginalFilename());
		System.out.println("-> " + destFile);
		
		file.transferTo(destFile);
		
		return "redirect:/tools.html?#c/imageupload";
	}

	@RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST}, value="/uploadmulti")
	@ResponseBody
	public Map<String, String> uploadMulti(MultipartHttpServletRequest req) throws IllegalStateException, IOException {
		
		for (Entry<String, MultipartFile> e : req.getFileMap().entrySet()) {
			System.out.println(e);

			File destFile = new File(this.uploadFir, e.getValue().getOriginalFilename());
			System.out.println("-> " + destFile);
			
			e.getValue().transferTo(destFile);
		}
		
		// return "redirect:/tools.html?#c/imageupload";
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("result", "ok");
		return map;
	}

}
