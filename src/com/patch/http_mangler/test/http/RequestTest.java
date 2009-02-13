package com.patch.http_mangler.test.http;

import static org.junit.Assert.*;

import org.junit.Test;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.MessageBuilder;
import com.patch.http_mangler.http.Request;

public class RequestTest {
	@Test
	public void testGetRequestLine() {
		Message message = buildMessage("GET http://www.example.com/index.html HTTP/1.1\r\nHost: www.example.com\r\n\r\n");
		Request request = new Request(message);
		assertEquals("GET http://www.example.com/index.html HTTP/1.1", request.getStartLine());
	}

	@Test
	public void testGetHost() {
		Message message = buildMessage("GET http://www.example.com/index.html HTTP/1.1\r\nHost: www.example.com\r\n\r\n");
		Request request = new Request(message);
		assertEquals("www.example.com", request.getHost());
	}
	
	@Test
	public void testGetHostWithPort() {
		Message message = buildMessage("GET http://www.example.com:80/index.html HTTP/1.1\r\nHost: www.example.com\r\n\r\n");
		Request request = new Request(message);
		assertEquals("www.example.com", request.getHost());
	}
	
	@Test
	public void testGetHostWithNoSlash() {
		Message message = buildMessage("GET http://www.example.com HTTP/1.1\r\nHost: www.example.com\r\n\r\n");
		Request request = new Request(message);
		assertEquals("www.example.com", request.getHost());
	}

	@Test
	public void testGetHostWithNoHttp() {
		Message message = buildMessage("CONNECT mail.google.com:443 HTTP/1.1\r\nHost: www.example.com\r\n\r\n");
		Request request = new Request(message);
		assertEquals("mail.google.com", request.getHost());
	}
	@Test
	public void testGetPortWhenNotSet() {
		Message message = buildMessage("GET http://www.example.com/index.html HTTP/1.1\r\nHost: www.example.com\r\n\r\n");
		Request request = new Request(message);
		assertEquals(80, request.getPort());
	}
	
	private Message buildMessage(String s) { // s is a sequence of bytes
		MessageBuilder mb = new MessageBuilder();
		for (int i = 0; i < s.length(); i++) {
			mb.feed((byte) s.charAt(i));
		}
		return mb.getMessage();
	}
}
