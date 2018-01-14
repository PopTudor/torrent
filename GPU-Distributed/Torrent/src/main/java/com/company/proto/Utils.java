package com.company.proto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
}
