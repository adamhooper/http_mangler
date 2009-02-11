package com.patch.http_mangler.proxy;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Request;

public class ProxiedClientRequest extends Request {
	public ProxiedClientRequest(Message message) {
		super(message);
	}
}
