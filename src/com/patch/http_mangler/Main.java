package com.patch.http_mangler;

import com.patch.http_mangler.net.ProxyServer;

public class Main {
	public static void main(String[] args) {
		Options options = new Options();
		if (args.length == 1 && "--cache".equals(args[0])) {
			options.cache = true;
		}
		
		System.out.println("Starting server on port 8080" + (options.cache ? " with crazy-caching" : ""));
		
		ProxyServer proxyServer = new ProxyServer(options);
		proxyServer.run();
	}

}
