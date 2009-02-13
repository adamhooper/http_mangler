package com.patch.http_mangler.http;

import java.util.ArrayList;

public class ChunkedDecoder {
	private final static int STATE_CHUNK_SIZE = 0;
	private final static int STATE_CHUNK_DATA = 1;
	private final static int STATE_CHUNK_LAST = 2;
	
	public byte[] decode(byte[] input) {
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		int inCrLf = 0;
		String chunkSizeString = "";
		int chunkSize = 0;
		int state = STATE_CHUNK_SIZE;
		
		for (byte b : input) {
			switch (state) {
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
					bytes.add(b);
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
		
		byte[] bytesArray = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			bytesArray[i] = bytes.get(i);
		}
		return bytesArray;
	}
}
