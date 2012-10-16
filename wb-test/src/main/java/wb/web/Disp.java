package wb.web;

import javax.servlet.http.HttpServletRequest;

public class Disp {

	public static String getRootUrl(HttpServletRequest req, String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(req.getScheme());
		sb.append("://");
		sb.append(req.getServerName());
		if (req.getServerPort() != -1 
				&& req.getServerPort() != getDefaultPort(req.getScheme())) {
			sb.append(":");
			sb.append(req.getServerPort());
		}
		sb.append("/");
		if (req.getContextPath() != null && req.getContextPath().length() != 0) {
			sb.append(removeFirstSlash(req.getContextPath()));
			sb.append("/");
		}
		sb.append(removeFirstSlash(path));
		return sb.toString();
	}

	public static String getServletUrl(HttpServletRequest req, String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(req.getScheme());
		sb.append("://");
		sb.append(req.getServerName());
		if (req.getServerPort() != -1 
				&& req.getServerPort() != getDefaultPort(req.getScheme())) {
			sb.append(":");
			sb.append(req.getServerPort());
		}
		sb.append("/");
		if (req.getContextPath() != null && req.getContextPath().length() != 0) {
			sb.append(removeFirstSlash(req.getContextPath()));
			sb.append("/");
		}
		if (req.getServletPath() != null && req.getServletPath().length() != 0) {
			sb.append(removeFirstSlash(req.getServletPath()));
			sb.append("/");
		}
		sb.append(removeFirstSlash(path));
		return sb.toString();
	}

	private static int getDefaultPort(String scheme) {
		if ("http".equalsIgnoreCase(scheme)) {
			return 80;
		}
		if ("https".equalsIgnoreCase(scheme)) {
			return 443;
		}
		return 0;
	}

	private static String removeFirstSlash(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

	public static String getRootUrl(HttpServletRequest req) {
		return getRootUrl(req, "/");
	}

}
