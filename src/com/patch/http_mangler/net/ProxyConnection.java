package com.patch.http_mangler.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import com.patch.http_mangler.http.Message;
import com.patch.http_mangler.http.MessageBuilder;
import com.patch.http_mangler.mangling.LoadingMetricsWebsiteMangler;
import com.patch.http_mangler.mangling.MangleListMangler;
import com.patch.http_mangler.proxy.ProxiedClientRequest;
import com.patch.http_mangler.proxy.ProxiedClientResponse;
import com.patch.http_mangler.proxy.ProxiedServerRequest;
import com.patch.http_mangler.proxy.ProxiedServerResponse;

public class ProxyConnection implements Runnable {
	Socket clientSocket;

	public ProxyConnection(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		Message clientRequestMessage;

		try {
			clientRequestMessage = createMessageFromSocket(clientSocket);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ProxiedClientRequest clientRequest = new ProxiedClientRequest(
				clientRequestMessage);
		ProxiedServerRequest serverRequest = createServerRequestFromClientRequest(clientRequest);

		String serverHost = serverRequest.getHost();
		int serverPort = serverRequest.getPort();

		Socket serverSocket;
		try {
			serverSocket = new Socket(serverHost, serverPort);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			sendMessageToSocket(serverSocket, serverRequest.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Message serverResponseMessage;

		try {
			serverResponseMessage = createMessageFromSocket(serverSocket);
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ProxiedServerResponse serverResponse = new ProxiedServerResponse(serverResponseMessage);
		ProxiedClientResponse clientResponse = createClientResponseFromServerResponse(serverResponse, clientRequest);

		try {
			sendMessageToSocket(clientSocket, clientResponse.getMessage());
			clientSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Message createMessageFromSocket(Socket socket) throws IOException {
		InputStream is = socket.getInputStream();
		
		MessageBuilder mb = new MessageBuilder();
		Message m;
		while ((m = mb.getMessage()) == null) {
			int c = is.read();
			if (c == -1) {
				throw new RuntimeException("Could not read from server but message is incomplete:" + mb);
			}
			mb.feed((byte) c);
		}
		
		System.out.println("Read: " + m.getStartLine());// + "\n" + m.getHeaders());
		
		return m;
	}

	private void sendMessageToSocket(Socket socket, Message message)
			throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		for (byte b : message) {
			bos.write(b);
		}
		bos.flush();

		System.out.println("Write: " + message.getStartLine());// + "\n" + message.getHeaders());
	}

	private ProxiedServerRequest createServerRequestFromClientRequest(
			ProxiedClientRequest clientRequest) {
		Message orig = clientRequest.getMessage();
		
		String serverHeaders = Pattern.compile("Accept-Encoding: gzip,deflate\\r\\n").matcher(orig.getHeaders()).replaceAll("");

		Message outMessage = new Message(orig.getStartLine(), serverHeaders, orig.getBody());
		
		return new ProxiedServerRequest(outMessage);
	}

	private ProxiedClientResponse createClientResponseFromServerResponse(
			ProxiedServerResponse serverResponse,
			ProxiedClientRequest originalRequest) {
		MangleListMangler mangleList = new MangleListMangler();
		mangleList.addMangler(new LoadingMetricsWebsiteMangler());
		
		Message serverResponseMessage = serverResponse.getMessage();
		Message clientResponseMessage = mangleList.mangleMessage(serverResponseMessage, originalRequest);
		
		return new ProxiedClientResponse(clientResponseMessage);
	}
}
