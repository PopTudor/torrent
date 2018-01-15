package com.company.proto;


import com.company.proto.handlers.Handler;
import com.company.proto.handlers.HandlerFactory;
import com.company.proto.torrent.Torrent;
import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	
	public static void main(String[] args) throws IOException {
		Torrent.Node node = Torrent.Node.newBuilder()
				.setHost("127.0.0.1")
				.setPort(5000)
				.build();
		InetAddress inetAddress = InetAddress.getByName(node.getHost());
		try (ServerSocket listener = new ServerSocket(node.getPort(), 50, inetAddress)) {
			System.out.printf("Listening on: %s:%d\n", listener.getLocalSocketAddress(), listener.getLocalPort());
			while (true) {
				try (Socket socket = listener.accept()) {
					InputStream input = new DataInputStream(socket.getInputStream());
					int len = input.read();
					System.out.println(len);
					byte[] request = ByteStreams.toByteArray(input);
					
					Torrent.Message message = Torrent.Message.parseFrom(request);
					System.out.println(message.toString());
					
					Handler handler = HandlerFactory.create(message);
					Torrent.Message response = handler.handle(message);
					
					OutputStream output = new DataOutputStream(socket.getOutputStream());
					output.write(response.toByteArray());
					output.flush();
					
					socket.close();
				}
			}
		}
	}
}
