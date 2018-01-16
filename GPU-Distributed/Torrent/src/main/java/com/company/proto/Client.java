package com.company.proto;

import com.google.protobuf.ByteString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.company.proto.torrent.Torrent.*;

public class Client {
	public static void main(String[] argv) throws IOException {
		Node node = Node.newBuilder()
				.setHost("localhost")
				.setPort(5003)
				.build();
		
		try (Socket socket = new Socket(node.getHost(), node.getPort())) {
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			
			Message message = uploadRequestTest();
			byte lenReq = Utils.messageLen(message);
			lenReq = littleEndian(lenReq);
			
			output.writeByte(lenReq);
			output.write(message.toByteArray(), 0, lenReq);
			output.flush();
			
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			byte lenRes = inputStream.readByte();
			byte[] data = new byte[lenRes];
			inputStream.readFully(data, 0, lenRes);
			
			
			Message resMessage = Message.parseFrom(data);
			System.out.println("response: " + resMessage);
			
			socket.close();
		}
	}
	
	private static Message uploadRequestTest() {
		UploadRequest build = UploadRequest.newBuilder()
				.setFilename("")
				.setData(ByteString.copyFromUtf8("uploadReqqqauest test"))
				.build();
		return Message.newBuilder()
				.setType(Message.Type.UPLOAD_REQUEST)
				.setUploadRequest(build)
				.build();
	}
	
	public static byte littleEndian(byte b) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[]{b});
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuffer.get();
	}
}
