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
					DataInputStream inputStream = new DataInputStream(socket.getInputStream());
					// ..... receive .....
					Torrent.Message message = readMessageFrom(inputStream);
					// ..... process .....
					Handler handler = HandlerFactory.create(message);
					Torrent.Message response = handler.handle(message);
					// ..... respond ......
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					writeMessageTo(response, output);
					
					socket.close();
				}
			}
		}
	}

	private void writeMessageTo(Torrent.Message response, DataOutputStream output) throws IOException {
		int len = response.getSerializedSize();
		byte[] data = response.toByteArray();
		System.out.println("****** response ******\nlen:" + len + "\n" + response.toString());
		output.writeInt((len));
		output.write(data, 0, len);
		output.flush();
	}
	
	private Torrent.Message readMessageFrom(DataInputStream inputStream) throws IOException {
		int len = inputStream.readInt();
		byte[] data = new byte[len];
		inputStream.readFully(data, 0, len);
		Torrent.Message message = Torrent.Message.parseFrom(data);
		System.out.println("****** request ******\nlen:" + len + "\n" + message.toString());
		return message;
	}
	
}
