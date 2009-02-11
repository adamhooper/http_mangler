package com.patch.http_mangler.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
	private ServerSocket serverSocket;
	
	public ProxyServer(int port) {
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void run() {
		while (true) {
			Socket socket;
			
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			ProxyConnection proxyConnection = new ProxyConnection(socket);
			new Thread(proxyConnection).start();
		}
	}
}
