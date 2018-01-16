package com.company.proto;

import com.company.proto.torrent.Torrent;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {
	public static byte messageLen(Torrent.Message message) {
		Integer len = message.getSerializedSize();
		return len.byteValue();
	}
	
	public void writeLittleEndian(OutputStreamWriter stream, byte[] bytes) {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		while (bb.hasRemaining()) {
			short v = bb.getShort();
			try {
				stream.write(v);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ByteString hashToMD5(byte[] bytes) {
		HashCode hashCode = Hashing.md5().hashBytes(bytes);
		return ByteString.copyFrom(hashCode.asBytes());
	}
}
