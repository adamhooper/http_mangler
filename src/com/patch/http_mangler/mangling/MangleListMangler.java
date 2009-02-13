package com.patch.http_mangler.mangling;

import java.util.ArrayList;
import java.util.List;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.Request;

public class MangleListMangler implements Mangler {
	private List<Mangler> manglers = new ArrayList<Mangler>();
	
	public void addMangler(Mangler mangler) {
		this.manglers.add(mangler);
	}

	public Message mangleMessage(Message original, Request originalRequest) {
		Message message = original;
	
		for (Mangler mangler : this.manglers) {
			message = mangler.mangleMessage(message, originalRequest);
		}
		
		return message;
	}
}
