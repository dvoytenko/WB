package wb.web.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import wb.util.IoHelper;

@Controller
@RequestMapping( { "/speech" })
public class SpeechController {
	
	@RequestMapping(method = RequestMethod.POST, value="/save")
	@ResponseBody
	public String save(HttpServletRequest req) throws IOException {
		File dir = new File("target");
		File file = new File(dir, "" + System.currentTimeMillis() + ".x");
		OutputStream out = new FileOutputStream(file);
		IoHelper.copy(req.getInputStream(), out);
		out.close();
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value={"/temp/{path:.*}"})
	@ResponseBody
	public void temp(@PathVariable("path") String path,
			HttpServletResponse resp) throws IOException {
		
		if (path.endsWith(".png")) {
			resp.setContentType("audio/***");
			ServletOutputStream out = resp.getOutputStream();
			final File tempDir = new File(System.getProperty("java.io.tmpdir"));
			InputStream in = new BufferedInputStream(new FileInputStream(
					new File(tempDir, path)));
			IoHelper.copy(in, out);
			in.close();
			return;
		}
		
		throw new RuntimeException("unknown temp file: " + path);
	}
	
}
