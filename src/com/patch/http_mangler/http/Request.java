package com.patch.http_mangler.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
	private Message message;
	private String host;
	private int port;
	
	private final static int DEFAULT_PORT = 80;
	private final static Pattern REQUEST_LINE_PATTERN = Pattern.compile("\\w+ (http://)?([^:/ ]*)(:\\d+)?"); 

	public Request(Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return this.message;
	}

	public String getStartLine() {
		return this.message.getStartLine();
	}
	
	public String getHost() {
		if (this.host == null) {
			this.populateHostAndPort();
		}
		return this.host;
	}
	
	public int getPort() {
		if (this.port == 0) {
			this.populateHostAndPort();
		}
		return this.port;
	}
	
	private void populateHostAndPort() {
		String line = this.getStartLine();
		Matcher matcher = REQUEST_LINE_PATTERN.matcher(line);
		if (!matcher.lookingAt()) {
			throw new RuntimeException("No match in HOST line: " + line);
		}
		this.host = matcher.group(2);
		
		String portString = matcher.group(3);
		if (portString != null && portString.length() > 0 && portString.charAt(0) == ':') {
			this.port = Integer.parseInt(portString.substring(1));
		} else {
			this.port = DEFAULT_PORT;
		}
	}
}
