package com.patch.http_mangler.mangling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Request;

public class LoadingMetricsWebsiteMangler extends WebsiteMangler {
	private static Map<String,String> filenameToJavascript = new ConcurrentHashMap<String, String>();
	
	@Override
	public boolean shouldMangle(Message message, Request originalRequest) {
		String contentType = message.getHeader("Content-Type");
		if (contentType == null) return false;
		return contentType.indexOf("text/html") == 0;
	}
	
	public boolean shouldMangleBodyAsString(Message message) {
		return true;
	}
	
	protected String mangleBodyString(Message message, String bodyString) {
		StringBuilder sb = new StringBuilder(bodyString);
		

		int headStartPos = sb.indexOf("<head>");
		if (headStartPos != -1) {
			sb.insert(headStartPos + 6, javascriptTag("window.HEAD_START_TIME = new Date();"));
		}
		
		int headEndPos = sb.lastIndexOf("</head>");
		if (headEndPos != -1) {
			String headEndString = javascriptTag("window.HEAD_END_TIME = new Date();");
			sb.insert(headEndPos, headEndString);
			headEndPos += headEndString.length();
		}
		
		int bodyStartPos = (headEndPos >= 0) ? sb.indexOf(">", headEndPos + 8) : -1;
		if (bodyStartPos != -1) {
			sb.insert(bodyStartPos + 1, javascriptTag("window.BODY_START_TIME = new Date();"));
		}
		
		int bodyEndPos = sb.lastIndexOf("</body>");
		if (bodyEndPos != -1) {
			sb.insert(bodyEndPos, javascriptFile("loading-metrics-website-mangler-body-end.js"));
			//sb.insert(bodyEndPos, javascriptTag("window.BODY_END_TIME = new Date(); alert('<head> load time: ' + (window.HEAD_END_TIME - window.HEAD_START_TIME) + 'ms\\n<body> load time: ' + (window.BODY_END_TIME - window.BODY_START_TIME) + 'ms');"));
		}
		
		return sb.toString();
	}
	
	protected String javascriptFile(String filename) {
		String ret = filenameToJavascript.get(filename);
		
		if (ret != null) return javascriptTag(ret);
		
		try {
			InputStream is = getClass().getResourceAsStream(filename);
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			
			char[] chars = new char[1024];
			int numChars;
			while ((numChars = br.read(chars)) > -1) {
				sb.append(chars, 0, numChars);
			}
			
			ret = sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		filenameToJavascript.put(filename, ret);
		return javascriptTag(ret);
	}
	
	protected String javascriptTag(String javascript) {
		return "<script type=\"text/javascript\"><!--//--><![CDATA[//><!--\n" + javascript + "\n//--><!]]></script>";
	}
}
