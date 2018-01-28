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
		return message;
	}
	
	public static void writeMessageTo(Torrent.Message response, DataOutputStream output) throws IOException {
		int len = response.getSerializedSize();
		byte[] data = response.toByteArray();
		output.writeInt(len);
		output.write(data, 0, len);
		output.flush();
	}
}
