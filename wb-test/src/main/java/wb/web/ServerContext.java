package wb.web;

import java.io.File;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

public class ServerContext implements ApplicationContextAware {

	private File workDir;

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		String workDirProp = System.getProperty("workDir");
		if (workDirProp != null) {
			this.workDir = new File(workDirProp);
		} else {
			this.workDir = (File) ((WebApplicationContext) context).getServletContext()
					.getAttribute("javax.servlet.context.tempdir");
		}
		System.out.println("workDir: " + this.workDir);
	}
	
	public File getWorkDir() {
		return this.workDir;
	}

}
