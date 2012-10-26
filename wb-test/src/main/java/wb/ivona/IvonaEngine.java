package wb.ivona;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import wb.model.TtsEngine;
import wb.model.Voice;
import wb.util.IoHelper;

public class IvonaEngine implements TtsEngine {

	@Override
	public String getId() {
		return "ivona";
	}

	@Override
	public String getName() {
		return "Ivona";
	}

	@Override
	public List<Voice> getVoices() {
		List<Voice> list = new ArrayList<Voice>();
		list.add(new Voice("en_us_jennifer", "Jennifer", null, "female", "en", "US", "en_US"));
		return list;
	}

	@Override
	public String getDefaultVoiceId() {
		return "en_us_jennifer";
	}

	@Override
	public void generateAudio(String text, String voiceId, OutputStream output)
			throws IOException {
		try {
			final String baseUrl = "http://www.ivona.com/api/saas/rest/";
			
			final String getTokenUrl = baseUrl + "tokens/";
			
			HttpURLConnection con = (HttpURLConnection) new URL(getTokenUrl).openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			OutputStream out = con.getOutputStream();
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write("email=dvoytenko@gmail.com");
			writer.close();
			
			InputStream in = con.getInputStream();
			String token = IoHelper.readText(in, "UTF-8");
			in.close();
			if (token.charAt(0) == '"') {
				token = token.substring(1, token.length() - 1);
			}
			System.out.println(token);
			
			final String speechUrl = baseUrl + "speechfiles/";
			con = (HttpURLConnection) new URL(speechUrl).openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			out = con.getOutputStream();
			writer = new OutputStreamWriter(out, "UTF-8");
			writer.write("token=" + token);
			String md5 = md5(md5(System.getProperty("ivona.pass")) + token); // md5(md5($password) + $token)
			System.out.println("md5: " + md5);
			writer.write("&md5=" + md5);
			writer.write("&text=" + enc(text));
			writer.write("&contentType=" + enc("text/plain"));
			writer.write("&voiceId=" + voiceId);
			writer.write("&codecId=" + enc("mp3/22050")); // pcm16/8000
			writer.close();
			
			in = con.getInputStream();
			String speechFile = IoHelper.readText(in, "UTF-8");
			in.close();
			System.out.println(speechFile);
			
			JSONObject js = new JSONObject(speechFile);
			String soundUrl = js.getString("soundUrl");
			System.out.println("soundUrl: " + soundUrl);
			
			URL url = new URL(soundUrl);
			IoHelper.copy(url.openStream(), output);
			
			/*
			{"fileId":"irao6wLxJS",
				"charactersPrice":150,
				"soundUrl":"http:\/\/www.ivona.com\/online\/fileSaas.php?fi=irao6wLxJS&ssi=2129&lang=en&e=1",
				"embedCode":"<div id=\"flashplayer\"><\/div><script type=\"text\/javascript\">var flashvars = {}; var d = new Date(); flashvars.source=\"http%3A%2F%2Fwww.ivona.com%2Fonline%2FfileSaas.php%3Ffi%3Dirao6wLxJS%26ssi%3D2129%26lang%3Den%26e%3D0\"; flashvars.configURL= \"http:\/\/static.ivona.com\/online\/static\/xml\/config.xml?timestamp=\"+d.getTime(); var saJsHost = ((\"https:\" == document.location.protocol) ? \"https:\/\/secure.ivona.com\/online\/static\/\" : \"http:\/\/static.ivona.com\/online\/static\/\"); document.write(unescape(\"%3Cscript src='\" + saJsHost  + \"js\/saPlayer.js?timestamp=\" + d.getTime()+ \"type='text\/javascript'%3E%3C\/script%3E\"));<\/script>"
			}
			 */
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String md5(String s) {
		if (s == null) {
			throw new NullPointerException();
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md.update(s.getBytes());
		byte[] md5sum = md.digest();
		BigInteger bigInt = new BigInteger(1, md5sum);
		String output = bigInt.toString(16);
		while (output.length() < 32) {
		    output = "0" + output;
		}		
		return output;
	}

	private static String enc(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
