package com.company.proto;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Client {
	public static void main(String[] argv) throws IOException {
		Torrent.Node node = Torrent.Node.newBuilder()
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
