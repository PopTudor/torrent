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

import static com.company.proto.UtilsIO.readMessageFrom;
import static com.company.proto.UtilsIO.writeMessageTo;


public class Server {
	
	public Server(String host, int port) throws IOException {
		Torrent.Node node = Torrent.Node.newBuilder()
				.setHost(host)
				.setPort(port)
				.build();
		InetAddress inetAddress = InetAddress.getByName(node.getHost());
		try (ServerSocket listener = new ServerSocket(node.getPort(), 0, inetAddress)) {
			System.out.printf("Listening on: %s:%d\n", listener.getLocalSocketAddress(), listener.getLocalPort());
			while (true) {
				try (Socket socket = listener.accept()) {
					DataInputStream input = new DataInputStream(socket.getInputStream());
					// ..... receive .....
					Torrent.Message message = readMessageFrom(input);
					// ..... process .....
					Handler handler = HandlerFactory.create(message, node);
					Torrent.Message response = handler.handle(message);
					// ..... respond ......
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					writeMessageTo(response, output);
					
					input.close();
					output.close();
					socket.close();
				}
			}
		}
	}
	
}
