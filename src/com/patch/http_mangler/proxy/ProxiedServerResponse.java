package com.patch.http_mangler.proxy;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Response;

public class ProxiedServerResponse extends Response {
	public ProxiedServerResponse(Message message) {
		super(message);
	}
}
