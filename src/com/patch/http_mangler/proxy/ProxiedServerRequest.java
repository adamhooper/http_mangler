package com.patch.http_mangler.proxy;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Request;

public class ProxiedServerRequest extends Request {
	public ProxiedServerRequest(Message message) {
		super(message);
	}
}
