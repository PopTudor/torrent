package com.company.proto;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server {
	
	public static void main(String[] args) throws IOException {
		Torrent.Node node = Torrent.Node.newBuilder()
				.setHost("localhost")
				.setPort(5001)
				.build();
		Message
		try (ServerSocket listener = new ServerSocket(node.getPort())) {
			System.out.printf("Listening on: %s:%d\n", listener.getLocalSocketAddress(), listener.getLocalPort());
			while (true) {
				try (Socket socket = listener.accept()) {
					InputStreamReader reader = new InputStreamReader(socket.getInputStream());
					int len = reader.read();
					System.out.println(len);
					socket.close();
				}
			}
		}
	}
}
