package com.company.proto;

import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Utils {
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
		return ByteString.copyFrom(Hashing.md5().hashBytes(bytes).toString(), Charset.defaultCharset());
	}
}
