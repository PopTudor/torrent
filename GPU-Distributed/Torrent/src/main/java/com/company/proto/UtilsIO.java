package com.company.proto;

import com.company.proto.torrent.Torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UtilsIO {
	public static Torrent.Message readMessageFrom(DataInputStream inputStream) throws IOException {
		int len = inputStream.readInt();
		byte[] data = new byte[len];
		inputStream.readFully(data, 0, len);
		Torrent.Message message = Torrent.Message.parseFrom(data);
		System.out.println("****** request ******\nlen:" + len + "\n" + message.toString());
		return message;
	}
	
	public static void writeMessageTo(Torrent.Message response, DataOutputStream output) throws IOException {
		int len = response.getSerializedSize();
		byte[] data = response.toByteArray();
		System.out.println("****** response ******\nlen:" + len + "\n" + response.toString());
		output.writeInt((len));
		output.write(data, 0, len);
		output.flush();
	}
}
