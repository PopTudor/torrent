package com.company.proto;

import com.company.proto.torrent.Torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UtilsIO {
	public static Torrent.Message readMessageFrom(DataInputStream inputStream) throws IOException {
		int len = inputStream.readInt();
		byte[] data = new byte[len];
		int readLen = inputStream.read(data, 0, len);
		if (readLen == -1) throw new IOException("error");
		return Torrent.Message.parseFrom(data);
	}
	
	public static void writeMessageTo(Torrent.Message response, DataOutputStream output) throws IOException {
		int len = response.getSerializedSize();
		byte[] data = response.toByteArray();
		output.writeInt(len);
		output.write(data, 0, len);
		output.flush();
	}
}
