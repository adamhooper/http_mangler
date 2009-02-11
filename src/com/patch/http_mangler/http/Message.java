package com.patch.http_mangler.http;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Message implements Iterable<Byte> {
	private String startLine;
	private String headers;
	private byte[] body;
	
	public Message(String startLine, String headers, byte[] body) {
		this.startLine = startLine;
		this.headers = headers;
		this.body = body;
	}
	
	public String getStartLine() {
		return this.startLine;
	}
	
	public String getHeaders() {
		return this.headers;
	}
	
	public byte[] getBody() {
		return this.body;
	}
	
	public class MessageIterator implements Iterator<Byte> {
		private Message message;
		private int startLinePosition;
		private int afterStartLinePosition;
		private int headerPosition;
		private int afterHeaderPosition;
		private int bodyPosition;

		private final static String afterStartLine = "\r\n";
		private final static String afterHeader = "\r\n";
		
		public MessageIterator(Message m) {
			this.message = m;
		}

		public boolean hasNext() {
			return peek(false) != null;
		}

		public Byte next() {
			Byte b = peek(true);
			if (b == null) {
				throw new NoSuchElementException();
			}
			return b;
		}

		public void remove() {
			Byte b = peek(true);
			if (b == null) {
				throw new IllegalStateException();
			}
		}
		
		private Byte peek(boolean alsoRemove) {
			Byte b = null;
			
			if (startLinePosition < this.message.startLine.length()) {
				b = (byte) this.message.startLine.charAt(startLinePosition);
				if (alsoRemove) {
					startLinePosition++;
				}
			} else if (afterStartLinePosition < afterStartLine.length()) {
				b = (byte) afterStartLine.charAt(afterStartLinePosition);
				if (alsoRemove) {
					afterStartLinePosition++;
				}
			} else if (headerPosition < this.message.headers.length()) {
				b = (byte) this.message.headers.charAt(headerPosition);
				if (alsoRemove) {
					headerPosition++;
				}
			} else if (afterHeaderPosition < afterHeader.length()) {
				b = (byte) afterHeader.charAt(afterHeaderPosition);
				if (alsoRemove) {
					afterHeaderPosition++;
				}
			} else if (bodyPosition < this.message.body.length) {
				b = this.message.body[bodyPosition];
				if (alsoRemove) {
					bodyPosition++;
				}
			}
			
			return b;
		}
	}
	
	public Iterator<Byte> iterator() {
		return new MessageIterator(this);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (byte b : this) {
			sb.append((char) b);
		}
		return sb.toString();
	}
}
