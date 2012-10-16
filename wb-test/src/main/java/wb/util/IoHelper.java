package wb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.PrivilegedAction;

public final class IoHelper {

	private static final String CHARSET = "charset";

	public static final String ENCODING_ISO_8859_1 = "ISO-8859-1";

	public static final String ENCODING_UTF8 = "UTF-8";

	public static final String ENCODING_UTF16 = "UTF-16";

	private IoHelper() {
		// no code
	}

	/**
	 * Returns line separator for this platform. Typically either "\n" or "\r\n".
	 */
	public static String getSystemLineSeparator() {
		// TODO optimize: only fetch once
		return java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				return System.getProperty("line.separator");
			}
		});
	}

	public static String readText(File file) throws IOException {
		return readText(file, null);
	}

	public static String readText(File file, String encoding) throws IOException {
		InputStream stream = new FileInputStream(file);
		try {
			return readText(stream, encoding);
		} finally {
			stream.close();
		}
	}

	/**
	 * Delagates to {@link #readText(Class, String, String)} with encoding = null (default system encoding)
	 * 
	 * @see #readText(Class, String, String)
	 */
	public static String readText(Class<?> baseClass, String path) throws IOException {
		return readText(baseClass, path, null);
	}

	/**
	 * Reads classpath resource into a single String. If encoding specified it's used for reading the resource, if not -
	 * default system encoding is used.
	 * <p>
	 * Additionally, this method normalizes line breaks. Only '\n' is used for end-of-line.
	 * 
	 * @see #readText(InputStream, String)
	 */
	public static String readText(Class<?> baseClass, String path, String encoding) throws IOException {
		InputStream in = baseClass.getResourceAsStream(path);
		if (in == null) {
			throw new FileNotFoundException(baseClass.getPackage().getName() + ":" + path);
		}
		try {
			return readText(in, encoding);
		} finally {
			in.close();
		}
	}

	/**
	 * Reads stream into a single String. If encoding specified it's used for reading the stream, if not - default
	 * system encoding is used.
	 * <p>
	 * Additionally, this method normalizes line breaks. Only '\n' is used for end-of-line.
	 * 
	 * @see #
	 */
	public static String readText(InputStream stream, String encoding) throws IOException {
		Reader reader;
		if (encoding != null) {
			reader = new InputStreamReader(stream, encoding);
		} else {
			reader = new InputStreamReader(stream);
		}
		return readText(new BufferedReader(reader));
	}

	/**
	 * Reads reader into a single String
	 * <p>
	 * Additionally, this method normalizes line breaks. Only '\n' is used for end-of-line.
	 * 
	 * @see #
	 */
	public static String readText(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int r;
		while ((r = reader.read()) != -1) {
			if (r == '\r') {
				// ignore
			} else {
				sb.append((char) r);
			}
		}
		return sb.toString();
	}

	/**
	 * Reads content of URL into a single string.
	 * 
	 * @see #readText(URLConnection, String)
	 */
	public static String readText(URL url) throws IOException {
		return readText(url.openConnection(), null);
	}

	/**
	 * Reads content of URL connection into a single string.
	 * 
	 * @see #readText(URLConnection, String)
	 */
	public static String readText(URLConnection con) throws IOException {
		return readText(con, null);
	}

	/**
	 * Reads content of URL connection into a single string.
	 * 
	 * @see #readText(URLConnection, String)
	 */
	public static String readText(URLConnection con, String encoding) throws IOException {
		InputStream in = con.getInputStream();
		try {
			if (encoding == null) {
				encoding = extractCharset(con.getContentType());
			}
			return readText(in, encoding);
		} finally {
			in.close();
		}
	}

	/**
	 * Writes text into a stream. If encoding is <code>null</code> default system encoding is used. If lineSeparator is
	 * <code>null</code> - default line separator is used.
	 * 
	 * @see #writeText(CharSequence, Writer, String)
	 */
	public static void writeText(CharSequence text, OutputStream stream, String encoding, String lineSeparator)
			throws IOException {
		Writer writer;
		if (encoding != null) {
			writer = new OutputStreamWriter(stream, encoding);
		} else {
			writer = new OutputStreamWriter(stream);
		}
		writer = new BufferedWriter(writer);
		writeText(text, writer, lineSeparator);
		writer.flush();
	}

	/**
	 * Writes text into a writer with default line separator.
	 * 
	 * @see #writeText(CharSequence, Writer, String)
	 * @see #getSystemLineSeparator()
	 */
	public static void writeText(CharSequence text, Writer writer) throws IOException {
		writeText(text, writer, null);
	}

	/**
	 * Writes text into a writer. If lineSeparator is <code>null</code> - default line separator is used.
	 */
	public static void writeText(CharSequence text, Writer writer, String lineSeparator) throws IOException {
		if (lineSeparator == null) {
			lineSeparator = getSystemLineSeparator();
		}
		BufferedReader r = new BufferedReader(new StringReader(text.toString()));
		String line;
		while ((line = r.readLine()) != null) {
			writer.write(line);
			writer.write(lineSeparator);
		}
	}

	/**
	 * Copy contents of the source input strean to target output stream.
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		int r;
		while ((r = in.read()) != -1) {
			out.write(r);
		}
	}

	public static String extractContentType(String contentType) {
		if (contentType == null || contentType.length() == 0) {
			return null;
		}

		int encInd = contentType.indexOf(';');
		String res = encInd != -1 ? contentType.substring(0, encInd).trim() : contentType.trim();
		return res.length() > 0 ? res : null;
	}

	public static String extractCharset(String contentType) {
		// example: text/html; charset=ISO-8859-4
		String res = null;
		int len = contentType != null ? contentType.length() : 0;
		if (len > 0) {
			int encInd = 0;
			while (encInd < len) {
				encInd = contentType.indexOf(';', encInd);
				if (encInd == -1) {
					break;
				}

				encInd++;
				for (; encInd < len; encInd++) {
					if (!Character.isWhitespace(contentType.charAt(encInd))) {
						break;
					}
				}
				if (encInd < len
						&& contentType.regionMatches(true, encInd, CHARSET, 0, CHARSET.length())) {
					encInd++;
					for (; encInd < len; encInd++) {
						if (contentType.charAt(encInd) == '=') {
							break;
						}
					}
					encInd++;
					if (encInd < len) {
						int encEnd = contentType.indexOf(';', encInd);
						if (encEnd == -1) {
							encEnd = len;
						}
						res = contentType.substring(encInd, encEnd).trim();
					}
					// exit, charset parameter found
					break;
				}
			}
		}
		return res;
	}

	public static void readFile(URL url, File targetFile) throws IOException {
		InputStream in = new BufferedInputStream(url.openStream());
		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				targetFile));
		try {
			copy(in, out);
		} finally {
			out.close();
		}
		in.close();
	}

	public static void copy(File sourceFile, File targetFile) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
			try {
				copy(in, out);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	public static Reader reader(HttpURLConnection con) throws IOException {
		String encoding = extractCharset(con.getContentType());
		Reader reader;
		if (encoding != null) {
			reader = new InputStreamReader(con.getInputStream(), encoding);
		} else {
			reader = new InputStreamReader(con.getInputStream());
		}
		return new BufferedReader(reader);
	}

}
