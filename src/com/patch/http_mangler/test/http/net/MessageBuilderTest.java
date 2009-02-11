package com.patch.http_mangler.test.http.net;

import static org.junit.Assert.*;

import org.junit.Test;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.MessageBuilder;

public class MessageBuilderTest {
	@Test
	public void testStartsEmpty() {
		MessageBuilder mb = new MessageBuilder();
		assertNull(mb.getMessage());
	}

	@Test
	public void testGetRequest() {
		MessageBuilder mb = buildMessageBuilder("GET http://www.google.com/ HTTP/1.1\r\nHost: www.google.com\r\n\r\n");
		Message m = mb.getMessage();
		assertNotNull(m);
		assertEquals("GET http://www.google.com/ HTTP/1.1", m.getStartLine());
		assertEquals("Host: www.google.com\r\n", m.getHeaders());
		assertEquals(0, m.getBody().length);
	}
	
	@Test
	public void testMultipleHeaders() {
		MessageBuilder mb = buildMessageBuilder("GET http://www.google.com/ HTTP/1.1\r\nHost: www.google.com\r\nAccept: text/html\r\n\r\n");
		Message m = mb.getMessage();
		assertEquals("Host: www.google.com\r\nAccept: text/html\r\n", m.getHeaders());
	}

	@Test
	public void testChunkedTransferEncoding() {
		String body = "19\r\nThis is some chunked data\r\n0\r\n";
		String bytes = "HTTP/1.1 200 OK\r\nServer: gws\r\nTransfer-Encoding: chunked\r\n\r\n" + body;
		MessageBuilder mb = buildMessageBuilder(bytes);
		Message m = mb.getMessage();
		assertEquals(body, new String(m.getBody()));
	}
	
	@Test
	public void testChunkedWithSpaceInChunkSize() {
		String body = "19 \r\nThis is some chunked data\r\n0\r\n";
		String bytes = "HTTP/1.1 200 OK\r\nServer: gws\r\nTransfer-Encoding: chunked\r\n\r\n" + body;
		MessageBuilder mb = buildMessageBuilder(bytes);
		Message m = mb.getMessage();
		assertEquals(body, new String(m.getBody()));
	}
	
	@Test
	public void testContentLengthEncoding() {
		String body = "1234567890";
		String bytes = "HTTP/1.1 200 OK\r\nServer: gws\r\nContent-Length: 10\r\n\r\n" + body;
		MessageBuilder mb = buildMessageBuilder(bytes);
		Message m = mb.getMessage();
		assertEquals(body, new String(m.getBody()));
	}
	
	@Test
	public void testContentLengthZero() {
		String bytes = "HTTP/1.1 200 OK\r\nServer: gws\r\nContent-Length: 0\r\n\r\n";
		MessageBuilder mb = buildMessageBuilder(bytes);
		Message m = mb.getMessage();
		assertEquals(bytes, m.toString());
	}
	
	private MessageBuilder buildMessageBuilder(String s) { // s is a sequence of bytes
		MessageBuilder mb = new MessageBuilder();
		for (int i = 0; i < s.length(); i++) {
			mb.feed((byte) s.charAt(i));
		}
		return mb;
	}
}
