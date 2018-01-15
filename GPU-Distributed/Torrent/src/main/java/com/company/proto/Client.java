package com.company.proto;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.company.proto.torrent.Torrent.*;

public class Client {
	public static void main(String[] argv) throws IOException {
		Node node = Node.newBuilder()
				.setHost("localhost")
				.setPort(5000)
				.build();
		
		try (Socket s = new Socket(node.getHost(), node.getPort())) {
			OutputStream output = new DataOutputStream(s.getOutputStream());
			LocalSearchRequest searchRequest = LocalSearchRequest.newBuilder()
					.setRegex("torrent")
					.build();
			
			Message message = Message.newBuilder()
					.setType(Message.Type.LOCAL_SEARCH_REQUEST)
					.setLocalSearchRequest(searchRequest)
					.build();
			byte len = messageLen(message);
			len = littleEndian(len);
			
			output.write(len);
			output.write(message.toByteArray());
			output.flush();
		}
		
	}
	
	public static byte messageLen(Message message) {
		Integer len = message.getSerializedSize();
		return len.byteValue();
	}
	
	public static byte littleEndian(byte b) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[]{b});
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuffer.get(0);
	}
}
