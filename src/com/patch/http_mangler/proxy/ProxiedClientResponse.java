package com.patch.http_mangler.proxy;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Response;

public class ProxiedClientResponse extends Response {
	public ProxiedClientResponse(Message message) {
		super(message);
	}
}
