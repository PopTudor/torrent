package com.company.proto;

import com.google.protobuf.ByteString;

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
		
		try (Socket socket = new Socket(node.getHost(), node.getPort())) {
			OutputStream output = new DataOutputStream(socket.getOutputStream());
			
			
			Message message = uploadRequestTest();
			byte len = messageLen(message);
			len = littleEndian(len);
			
			output.write(len);
			output.flush();
			output.write(message.toByteArray());
			output.flush();

//			BufferedReader in =
//					new BufferedReader(
//							new InputStreamReader(socket.getInputStream()));
//			String fromServer = in.readLine();
//			InputStream input = new DataInputStream(socket.getInputStream());
//			byte[] request = ByteStreams.toByteArray(input);

//			Message responseMessage = Message.parseFrom(fromServer.getBytes());
//			System.out.println("response: "+responseMessage);
		}
	}
	
	private static Message uploadRequestTest() {
		UploadRequest build = UploadRequest.newBuilder()
				.setFilename("salut")
				.setData(ByteString.copyFromUtf8("uploadReqauest test"))
				.build();
		return Message.newBuilder()
				.setType(Message.Type.UPLOAD_REQUEST)
				.setUploadRequest(build)
				.build();
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
