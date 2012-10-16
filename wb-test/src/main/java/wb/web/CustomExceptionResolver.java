package wb.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class CustomExceptionResolver extends SimpleMappingExceptionResolver {
	
//	private final Logger warnLogger = LoggerFactory.getLogger("com.trader.web.app");

	@Override
	protected void logException(Exception ex, HttpServletRequest request) {
		// warnLogger.error(buildLogMessage(ex, request), ex);
		System.out.println("ERROR: " + ex);
		ex.printStackTrace(System.out);
	}

}
