package com.patch.http_mangler.mangling;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Request;

public interface Mangler {
	public Message mangleMessage(Message message, Request originalRequest);
}
