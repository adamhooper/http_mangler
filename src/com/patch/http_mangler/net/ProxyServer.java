package com.patch.http_mangler.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.patch.http_mangler.Options;

public class ProxyServer {
	private Options options;
	private ServerSocket serverSocket;
	
	public ProxyServer(Options options) {
		this.options = options;
		try {
			this.serverSocket = new ServerSocket(options.port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void run() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		while (true) {
			Socket socket;
			
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			ProxyConnection proxyConnection = new ProxyConnection(socket, options);
			executorService.execute(proxyConnection);
		}
	}
}
