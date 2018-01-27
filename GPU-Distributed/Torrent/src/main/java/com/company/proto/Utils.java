package com.company.proto;

import com.company.proto.torrent.Torrent;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utils {
	public static byte messageLen(Torrent.Message message) {
		Integer len = message.getSerializedSize();
		return len.byteValue();
	}
	
	public static boolean isValid(String regex) {
		try {
			if (regex.isEmpty()) throw new PatternSyntaxException("Empty regex", regex, 0);
			Pattern.compile(regex);
			return true;
		} catch (PatternSyntaxException exception) {
			return false;
		}
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
	
	public static Iterable<Torrent.ChunkInfo> toList(ByteString bytes) {
		return toList(bytes.toByteArray());
	}
	
	public static Iterable<Torrent.ChunkInfo> toList(byte[] data) {
		List<Torrent.ChunkInfo> chunkInfos = new ArrayList<>();
		for (int i = 0, index = 0; i < data.length; i += Constants.CHUNK_SIZE, index++) {
			byte[] chunk = Arrays.copyOfRange(data, i, Math.min(data.length, i + Constants.CHUNK_SIZE));
			
			Torrent.ChunkInfo chunkInfo = Torrent.ChunkInfo
					.newBuilder()
					.setIndex(index)
					.setSize(chunk.length)
					.setHash(Utils.hashToMD5(chunk))
					.build();
			chunkInfos.add(chunkInfo);
		}
		return chunkInfos;
	}
}
