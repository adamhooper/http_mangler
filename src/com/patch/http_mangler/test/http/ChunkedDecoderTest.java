package com.patch.http_mangler.test.http;

import static org.junit.Assert.*;

import org.junit.Test;

import java.nio.charset.Charset;

import com.patch.http_mangler.http.ChunkedDecoder;


public class ChunkedDecoderTest {
	@Test
	public void testDecode() {
		testOriginalAndExpected("19\r\nThis is some chunked data\r\n0\r\n", "This is some chunked data");
	}
	
	@Test
	public void testDecodeWithSpaceInSize() {
		testOriginalAndExpected("19 \r\nThis is some chunked data\r\n0\r\n", "This is some chunked data");
	}
	
	private void testOriginalAndExpected(String originalString, String expectedString) {
		byte[] original = bytesFromString(originalString);
		byte[] expected = bytesFromString(expectedString); 
		byte[] actual = new ChunkedDecoder().decode(original);
		assertArrayEquals(expected, actual);
	}
	
	private byte[] bytesFromString(String string) {
		return string.getBytes(Charset.forName("utf-8"));
	}
}
