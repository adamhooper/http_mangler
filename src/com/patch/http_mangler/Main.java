package com.patch.http_mangler;

import com.patch.http_mangler.net.ProxyServer;

public class Main {
	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		
		ProxyServer proxyServer = new ProxyServer(port);
		proxyServer.run();
	}

}
