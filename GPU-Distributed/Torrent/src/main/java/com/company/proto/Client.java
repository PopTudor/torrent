package com.company.proto;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static com.company.proto.torrent.Torrent.Node;

public class Client {
	public static void main(String[] argv) throws IOException {
		Node node = Node.newBuilder()
				.setHost("localhost")
				.setPort(5001)
				.build();
		
		try (Socket s = new Socket(node.getHost(), node.getPort())) {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(s.getOutputStream());
			outputStreamWriter.write(5);
			outputStreamWriter.flush();
		}
	}
}
