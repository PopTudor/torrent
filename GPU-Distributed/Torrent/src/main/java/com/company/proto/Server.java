package com.company.proto;


import com.company.proto.handlers.Handler;
import com.company.proto.handlers.HandlerFactory;
import com.company.proto.torrent.Torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	
	public static void main(String[] args) throws IOException {
		Torrent.Node node = Torrent.Node.newBuilder()
				.setHost("127.0.0.1")
				.setPort(5001)
				.build();
		InetAddress inetAddress = InetAddress.getByName(node.getHost());
		try (ServerSocket listener = new ServerSocket(node.getPort(), 0, inetAddress)) {
			System.out.printf("Listening on: %s:%d\n", listener.getLocalSocketAddress(), listener.getLocalPort());
			while (true) {
				try (Socket socket = listener.accept()) {
					DataInputStream inputStream = new DataInputStream(socket.getInputStream());
					byte len = inputStream.readByte();
					System.out.println(len);
					byte[] data = new byte[len];
					inputStream.readFully(data, 0, len);
					Torrent.Message message = Torrent.Message.parseFrom(data);
					System.out.println(message.toString());
					
					Handler handler = HandlerFactory.create(message);
					Torrent.Message response = handler.handle(message);
					
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					byte lenResponse = Utils.messageLen(response);
					output.write(lenResponse);
					output.write(response.toByteArray(), 0, lenResponse);
					
					socket.close();
				}
			}
		}
	}
}
