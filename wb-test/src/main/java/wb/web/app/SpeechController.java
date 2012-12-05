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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import wb.util.IoHelper;
import wb.web.ServerContext;

@Controller
@RequestMapping( { "/speech" })
public class SpeechController {
	
	@Autowired
	private ServerContext serverContext;
	
	@RequestMapping(method = RequestMethod.POST, value="/save/{path:.*}")
	@ResponseBody
	public String save(@PathVariable("path") String path,
			HttpServletRequest req) throws IOException {
		File file = new File(serverContext.getWorkDir(), path);
		System.out.println("record speech: " + file);
		OutputStream out = new FileOutputStream(file);
		IoHelper.copy(req.getInputStream(), out);
		out.close();
		return null;
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value={"/temp/{path:.*}"})
	public void temp(@PathVariable("path") String path,
			HttpServletResponse resp) throws IOException {
		
		if (path.endsWith(".wav")) {
			resp.setContentType("audio/wav");
			File file = new File(serverContext.getWorkDir(), path);
			resp.setContentLength((int) file.length());
			ServletOutputStream out = resp.getOutputStream();
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			IoHelper.copy(in, out);
			in.close();
			out.close();
			return;
		}
		
		throw new RuntimeException("unknown temp file: " + path);
	}
	
}
