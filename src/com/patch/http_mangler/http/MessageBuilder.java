package com.patch.http_mangler.http;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBuilder {
	private StringBuilder startLine = new StringBuilder();
	private StringBuilder headers = new StringBuilder();
	private List<Byte> body = new ArrayList<Byte>();
	private BodyParser bodyParser;
	private Message message;
	private int state = STATE_START_LINE;
	private int inCrLf = 0;

	private final static int STATE_START_LINE = 0;
	private final static int STATE_HEADERS = 1;
	private final static int STATE_BODY = 2;
	private final static int STATE_FINISHED = 3;
	
	private interface BodyParser {
		public void feed(byte b);
		public List<Byte> getBytes();
		public boolean isFinished();
	}
	
	private class ChunkedDecoder implements BodyParser {
		private List<Byte> bytes = new ArrayList<Byte>();
		private int inCrLf = 0;
		private String chunkSizeString = "";
		private int chunkSize = 0;
		private int state = STATE_CHUNK_SIZE;
		
		private final static int STATE_CHUNK_SIZE = 0;
		private final static int STATE_CHUNK_DATA = 1;
		private final static int STATE_CHUNK_LAST = 2;
		
		public void feed(byte b) {
			bytes.add(b);
			
			switch (this.state) {
			case STATE_CHUNK_SIZE:
				if (b == '\r' || b == '\n') {
					inCrLf++;
					if (inCrLf == 2) {
						inCrLf = 0;
						chunkSize = Integer.parseInt(chunkSizeString.trim(), 16);
						if (chunkSize == 0) {
							state = STATE_CHUNK_LAST;
						} else {
							state = STATE_CHUNK_DATA;
						}
					}
				} else {
					chunkSizeString += (char) b;
				}
				break;
			case STATE_CHUNK_DATA:
				if (chunkSize > 0) {
					chunkSize--;
				} else {
					if (inCrLf == 0 && b == '\r') {
						inCrLf++;
					} else if (inCrLf == 1 && b == '\n') {
						inCrLf = 0;
						chunkSizeString = "";
						state = STATE_CHUNK_SIZE;
					} else {
						throw new RuntimeException("Invalid chunkiness");
					}
				}
				break;
			case STATE_CHUNK_LAST:
				/*
				 * Okay, this is a backwards parser. If we got here, that means the
				 * last STATE_CHUNK_SIZE was actually STATE_CHUNK_LAST. So any bytes
				 * that come in here are evil.
				 */
				throw new RuntimeException("Past the last byte!");
			}
		}
		
		public List<Byte> getBytes() {
			return this.bytes;
		}
		
		public boolean isFinished() {
			return this.state == STATE_CHUNK_LAST;
		}
	}
	
	private class LengthDecoder implements BodyParser {
		private List<Byte> bytes = new ArrayList<Byte>();
		private int length;
		
		public LengthDecoder(int length) {
			this.length = length;
		}

		public void feed(byte b) {
			if (length == 0) {
				throw new RuntimeException("Too long!");
			}
			
			this.bytes.add(b);
			length--;
		}

		public List<Byte> getBytes() {
			return this.bytes;
		}

		public boolean isFinished() {
			return length == 0;
		}
	}
	
	public void feed(byte b) {
		switch (this.state) {
		case STATE_START_LINE:
			if (b == '\r') {
				inCrLf++;
			} else if (inCrLf > 0 && b == '\n') {
				inCrLf = 0;
				goToNextState();
			} else {
				inCrLf = 0;
				this.startLine.append((char) b);
			}
			break;
		case STATE_HEADERS:
			if (b == '\r') {
				inCrLf++;
			} else if (inCrLf > 0 && b == '\n') {
				inCrLf++;
				if (inCrLf == 4) { // \r\n\r\n
					goToNextState();
				}
			} else {
				inCrLf = 0;
			}
			
			if (inCrLf < 3) {
				this.headers.append((char) b);
			}
			break;
		case STATE_BODY:
			this.bodyParser.feed(b);
			if (this.bodyParser.isFinished()) {
				goToNextState();
			}
			break;
		case STATE_FINISHED:
			throw new RuntimeException("Extra byte: " + b + "(so far: '" + new String(this.getBodyBytes()) + "')");
		}
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	private int nextState() {
		switch (this.state) {
		case STATE_START_LINE:
			return STATE_HEADERS;
		case STATE_HEADERS:
			if (getHeader("Transfer-Encoding") != null) {
				// assume "chunked"
				this.bodyParser = new ChunkedDecoder();
				this.body = this.bodyParser.getBytes();
				return STATE_BODY;
			} else if (getHeader("Content-Length") != null) {
				int length = Integer.parseInt(getHeader("Content-Length"));
				this.bodyParser = new LengthDecoder(length);
				this.body = this.bodyParser.getBytes();
				if (this.bodyParser.isFinished()) {
					return STATE_FINISHED;
				}
				return STATE_BODY;
			}
			return STATE_FINISHED;
		case STATE_BODY:
			return STATE_FINISHED;
		default:
			throw new RuntimeException("No next state!");
		}
	}
	
	private String getHeader(String name) {
		Pattern p = Pattern.compile("(^|\\n)" + name + ":(.*)(\\r|$)");
		Matcher m = p.matcher(headers);
		if (m.find()) {
			return m.group(2).trim();
		} else {
			return null;
		}
	}
	
	private byte[] getBodyBytes() {
		byte[] bs = new byte[body.size()];
		for (int i = 0; i < body.size(); i++) {
			bs[i] = body.get(i);
		}
		return bs;
	}
	
	private void goToNextState() {
		state = nextState();
		
		if (state == STATE_FINISHED) {
			this.message = new Message(startLine.toString(), headers.toString(), getBodyBytes());
		}
	}
	
	public String toString() {
		return new Message(startLine.toString(), headers.toString(), getBodyBytes()).toString();
	}
}
