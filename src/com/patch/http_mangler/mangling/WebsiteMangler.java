package com.patch.http_mangler.mangling;

import java.nio.charset.Charset;

import com.patch.http_mangler.http.ChunkedDecoder;
import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Request;

public abstract class WebsiteMangler implements Mangler {
	private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
	
	protected boolean shouldMangle(Message message, Request originalRequest) {
		return false;
	}

	protected boolean shouldMangleBodyAsString(Message message) {
		return false;
	}
	
	protected String mangleStartLine(Message message) {
		return message.getStartLine();
	}
	
	protected String mangleHeaders(Message message, byte[] mangledBody) {
		if (shouldMangleBodyAsString(message)) {
			String original = message.getHeaders();
			String withContentLength;
			if ("chunked".equals(message.getHeader("Transfer-Encoding"))) {
				withContentLength = original.replaceFirst("Transfer-Encoding:[^\r]+", "Content-Length: " + mangledBody.length);
			} else {
				withContentLength = original.replaceFirst("Content-Length:[^\r]+", "Content-Length: " + mangledBody.length);
			}
			return withContentLength;
		} else {
			return message.getHeaders();
		}
	}
	
	protected byte[] mangleBody(Message message) {
		if (shouldMangleBodyAsString(message)) {
			byte[] originalBytes = message.getBody();
			byte[] unchunkedBytes;
			if ("chunked".equals(message.getHeader("Transfer-Encoding"))) {
				unchunkedBytes = new ChunkedDecoder().decode(originalBytes);
			} else {
				unchunkedBytes = originalBytes;
			}
			Charset charset = getCharset(message);
			String bodyString = new String(unchunkedBytes, charset);
			String newBodyString = mangleBodyString(message, bodyString);
			return newBodyString.getBytes(charset);
		} else {
			return message.getBody();
		}
	}
	
	protected String mangleBodyString(Message message, String bodyString) {
		return bodyString;
	}
	
	protected Charset getCharset(Message message) {
		String contentType = message.getHeader("Content-Type");
		if (contentType == null) return DEFAULT_CHARSET;
		
		// "Content-Type: text/html; charset=utf-8"
		int equalsPos = contentType.lastIndexOf('=');
		if (equalsPos <= 0) return DEFAULT_CHARSET;
		
		Charset charset = Charset.forName(contentType.substring(equalsPos + 1));
		if (charset == null) return DEFAULT_CHARSET;
		
		return charset;
	}
	
	@Override
	public Message mangleMessage(Message message, Request originalRequest) {
		if (shouldMangle(message, originalRequest)) {
			String startLine = mangleStartLine(message);
			byte[] body = mangleBody(message);
			String headers = mangleHeaders(message, body);
			
			return new Message(startLine, headers, body);
		} else {
			return message;
		}
	}
}
