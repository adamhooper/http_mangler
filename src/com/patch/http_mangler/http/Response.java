package com.patch.http_mangler.http;

public class Response {
	private Message message;
	
	public Response(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return this.message;
	}
}
